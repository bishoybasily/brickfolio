# Brickfolio

Brickfolio keeps track of off-plan real-estate offers and checks whether you can afford them.

- **Offer intake chat.** Paste a broker's message (Arabic or English). An LLM pulls out the developer, project, unit and payment plans. It matches them against what is already stored and creates whatever is missing, asking you first when it isn't sure.
- **Cash-burn simulation.** Given a unit's price and a payment plan, it simulates month by month how long your savings last. Savings earn monthly interest, and the simulation tracks scheduled milestones, regular installments, an initial deposit and monthly contributions. A unit can be checked against every payment plan of its project in one call. See [Cash-burn simulation](#cash-burn-simulation).

## Cash-burn simulation

`CashBurnService` answers one question: if I keep my seed cash in a savings account and pay the developer out of it, does the money last until the unit is fully paid, and what is left at the end?

The idea behind it is that off-plan payment plans are interest-free loans from the developer. Every pound you don't pay upfront stays in the bank and earns interest. The interest, plus whatever you add each month, pays part of the installments. Choosing the plan well lets a smaller seed buy the same unit, or the same seed end with more money.

### How the simulation works

1. **Effective price.** `unitPrice × (1 − initialDiscountPercentage / 100)`. Plans that ask for more cash upfront usually come with a bigger discount.
2. **Split the price.** `scheduledPayments` are fixed percentages of the effective price due on a given month. Month `0` is the down payment, and other months are milestones such as delivery. Whatever is left (`100% − sum of scheduled`) is financed and split evenly over `installmentMonths`. That gives `monthlyInstallment`.
3. **Month 0.** The balance starts at `initialDeposit` and the down payment is taken out of it.
4. **Months 1 … `installmentMonths`.** Each month:
   - any scheduled payment due that month is paid;
   - every `installmentIntervalMonths` months (1 = monthly, 3 = quarterly, …) the installments for that period are paid (`monthlyInstallment × interval`). The last month pays whatever is left, so 100 months paid quarterly ends with a 1-month installment at month 100;
   - if the balance is now zero or below, the simulation stops: you ran out of cash;
   - otherwise the balance earns one month of interest (15% a year, so 1.25% a month) on what is left after this month's payments, and then `monthlyDeposit` is added.
5. It stops early once everything owed to the developer is paid.

The response gives:

| Field | Meaning |
|---|---|
| `effectiveUnitPrice` | Price after the plan's discount |
| `monthlyInstallment` | Financed amount ÷ `installmentMonths` |
| `monthsSurvived` | Months the balance stayed positive |
| `totalPaidToDeveloper` / `totalUnpaidToDeveloper` | How much of the effective price was paid before the simulation ended |
| `remainingBalance` | What is left in the bank at the end. It is negative if the cash ran out |
| `paidAmounts` | Every payment, grouped by month |

A plan is affordable when `totalUnpaidToDeveloper` is `0` **and** `remainingBalance` is positive. The last payment can clear the debt but still overdraw the account, and that does not count.

`assessUnitSimulateCashBurn` takes a unit id, a seed (`initialDeposit`), a `monthlyDeposit` and an installment interval. It runs the simulation once for each payment plan of the unit's project, using the plan's discount, payments and length, so you can compare the plans side by side.

### Getting the most out of the seed cash

The simulation gives you the numbers to make three decisions:

- **Pick the plan that suits your cash, not the one with the biggest discount.** A large discount looks attractive, but it comes with a large down payment and short installments. That money stops earning interest straight away. A long plan with a small down payment leaves most of the seed in the bank, where the interest pays a growing share of each installment.
- **Find the smallest seed that works.** Run the same plan again with a lower `initialDeposit` until it stops being affordable. Whatever you don't need for the unit stays free for something else, such as a second unit, an emergency fund or another investment.
- **Find how much to add each month.** For a given seed, raise `monthlyDeposit` until the plan becomes affordable. That tells you how much of your income the unit will tie up.

#### Example

Take a unit at 5,000,000 EGP, quarterly installments and 25,000 EGP saved each month. Here are three plans:

| Plan | Effective price | Smallest seed that pays it off | With a 3,000,000 seed |
|---|---|---|---|
| **B:** 15% off, 50% down, rest over 2 years | 4,250,000 | ≈ 3,450,000 | runs out in month 14 with 796,875 still owed |
| **A:** 10% down, rest over 8 years | 5,000,000 | ≈ 1,730,000 | fully paid, ≈ 4,220,000 left after 8 years |
| **C:** 5% down, 5% at month 12, rest over 10 years | 5,000,000 | ≈ 1,250,000 | fully paid, ≈ 7,800,000 left after 10 years |

Plan B saves 750,000 EGP on the price, yet it needs almost three times the seed of plan C. Plan C buys the same unit with 1,250,000 EGP, because the interest on the money still in the bank pays a large part of the installments. Each plan reports `remainingBalance` at the end of its own term, so for a fair comparison of A and C, look at both the seed they need and how long they tie your money up.

### Assumptions and limits

- The savings rate is fixed at 15% a year (`calculateMonthlyInterest`). It does not model rate changes, taxes or inflation.
- Interest is earned on the balance after the month's payments. The monthly deposit starts earning interest the following month.
- `installmentIntervalMonths` must be at least 1.
- Scheduled payments must add up to less than 100%, and none can fall after `installmentMonths`. Only one scheduled payment per month is counted.
- If the down payment is larger than `initialDeposit`, the plan fails right away (`monthsSurvived = 0`).

## Architecture

```
React UI ──HTTP / WebSocket──▶ Spring GraphQL (WebFlux)
                                 ├─ queries ─────────▶ blocking services ──▶ JPA ──▶ Postgres (Flyway)
                                 └─ parseOffer subscription
                                        └─ Spring AI ChatClient (streaming, Ollama)
                                              ├─ chat memory ──▶ conversations table
                                              └─ @Tool methods ──▶ the same services
```

- **GraphQL over WebFlux, blocking underneath.** Controllers and services are plain blocking code. Spring GraphQL runs blocking controller and `@BatchMapping` methods on the `applicationTaskExecutor`. Here that is the virtual-thread `ioExecutor` from `ConcurrencyConfig`, so blocking calls never run on the Netty event loop.
- **Streaming chat.** `parseOffer` is the only reactive endpoint. It streams the model's answer as a `Flux<String>` over the GraphQL WebSocket. It subscribes on the virtual-thread scheduler from `SchedulersProvider`, because chat memory and the tools are blocking JPA calls.
- **AI tools.** `ToolsService` exposes `listDevelopers`, `createDeveloper`, `listProjects`, `createProject`, `listUnits`, `createUnit`, `listPaymentPlans` and `createPaymentPlan` to the model. Failures come back to the model as an `ERROR: …` string instead of an exception. The system prompt in `AiConfig` describes the matching workflow and the extraction rules.
- **Persistence.** All services extend `_JpaService`, which provides filtering, sorting and paging through `Params` → `Specification`. Single repository calls rely on Spring Data's own transactions, and multi-step writes use the `writeTransactionTemplate`. Repositories fetch the associations that DTO mapping and GraphQL need through `@EntityGraph`. Lazy loading outside a transaction is off, and nested GraphQL fields are resolved in batches.
- **Schema.** `src/main/resources/db/migration` (Flyway) owns the schema. Hibernate only validates it (`ddl-auto: validate`).

## Tech stack

Java 25, Spring Boot 4.1 (WebFlux, GraphQL, Data JPA, Flyway, Actuator), Spring AI 2.0 with Ollama, PostgreSQL 16, Lombok, Testcontainers, Spotless (Eclipse formatter). Frontend: React 19, TypeScript, Vite, Apollo Client, MUI, Tailwind.

## Running locally

Requirements: JDK 25, Docker, Node.js (for the UI), and a running [Ollama](https://ollama.com) with the configured model pulled.

```bash
cp .env.example .env            # then edit the values
docker compose --env-file .env -f docker/compose/dependencies.yaml up -d   # Postgres
ollama pull gemma4:31b          # or whatever OLLAMA_MODEL is set to
./gradlew bootRun               # API on http://localhost:8080
```

The app reads `.env` from the working directory on start (`spring.config.import`). Every setting has a local default.

| Variable | Default |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME` | `127.0.0.1`, `5432`, `brickfolio` |
| `DB_USERNAME`, `DB_PASSWORD` | `postgres`, `postgres` |
| `OLLAMA_BASE_URL`, `OLLAMA_MODEL` | `http://localhost:11434`, `gemma4:31b` |

GraphiQL is available at http://localhost:8080/graphiql. The GraphQL schema is in `src/main/resources/graphql/schema.graphqls`.

To use another model provider, replace `spring-ai-starter-model-ollama` in `build.gradle` with that provider's starter and pass its API key through an environment variable, for example `spring.ai.openai.api-key: ${OPENAI_API_KEY}`.

### Frontend

```bash
cd frontend
npm install
npm run dev                     # http://localhost:5173, proxies /graphql (HTTP + WebSocket) to :8080
```

The main view lists every unit with its project and payment plans, and runs the cash-burn simulation for each one against adjustable thresholds. A floating button opens the offer-intake chat and a manual simulation form.

## Tests

```bash
./gradlew test                  # needs Docker running
./gradlew spotlessApply         # format before committing
```

- Unit tests with no Spring: the `CashBurnService` maths, `ToolsService` error handling, `Payment` equality and `SchedulersProvider`.
- Integration tests (`PostgresIntegrationTest`): boot the whole application against a Postgres Testcontainer. Flyway migrates and Hibernate validates. They cover the services (CRUD, filters, paging, the create-under-a-parent methods, updates) and the GraphQL API (batch-loaded nested fields, `NOT_FOUND` errors).
