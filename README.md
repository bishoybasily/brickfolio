# Brickfolio

Brickfolio keeps track of off-plan real-estate offers and checks whether you can afford them.

- **Offer intake chat.** Paste a broker's message (Arabic or English). An LLM pulls out the developer, project, unit and payment plans. It matches them against what is already stored and creates whatever is missing, asking you first when it isn't sure.
- **Cash-burn simulation.** Given a unit's price and a payment plan, it simulates month by month how long your savings last. Savings earn monthly interest, and the simulation tracks scheduled milestones, regular installments, an initial deposit and monthly contributions. A unit can be checked against every payment plan of its project in one call.

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
