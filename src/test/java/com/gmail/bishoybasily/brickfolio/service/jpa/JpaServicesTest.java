package com.gmail.bishoybasily.brickfolio.service.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;

import jakarta.persistence.EntityNotFoundException;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;

import com.gmail.bishoybasily.brickfolio.PostgresIntegrationTest;
import com.gmail.bishoybasily.brickfolio.domain.dto.AssesUnitCashBurnRequestDto;
import com.gmail.bishoybasily.brickfolio.domain.dto.CashBurnResponseDto;
import com.gmail.bishoybasily.brickfolio.domain.dto.DeveloperDto;
import com.gmail.bishoybasily.brickfolio.domain.dto.PaymentPlanDto;
import com.gmail.bishoybasily.brickfolio.domain.dto.ProjectDto;
import com.gmail.bishoybasily.brickfolio.domain.dto.UnitDto;
import com.gmail.bishoybasily.brickfolio.domain.entity.*;
import com.gmail.bishoybasily.brickfolio.domain.parmas.DeveloperParams;
import com.gmail.bishoybasily.brickfolio.domain.parmas.PaymentPlanParams;
import com.gmail.bishoybasily.brickfolio.domain.parmas.ProjectParams;
import com.gmail.bishoybasily.brickfolio.domain.parmas.UnitParams;
import com.gmail.bishoybasily.brickfolio.domain.repository.*;
import com.gmail.bishoybasily.brickfolio.service.CashBurnService;
import com.gmail.bishoybasily.brickfolio.service.ToolsService;

class JpaServicesTest extends PostgresIntegrationTest {

  @Autowired
  DevelopersService developersService;
  @Autowired
  ProjectsService projectsService;
  @Autowired
  UnitsService unitsService;
  @Autowired
  PaymentPlansService paymentPlansService;
  @Autowired
  ConversationsService conversationsService;
  @Autowired
  ToolsService toolsService;
  @Autowired
  CashBurnService cashBurnService;

  @Autowired
  PaymentPlansRepository paymentPlansRepository;
  @Autowired
  UnitsRepository unitsRepository;
  @Autowired
  ProjectsRepository projectsRepository;
  @Autowired
  DevelopersRepository developersRepository;
  @Autowired
  ConversationsRepository conversationsRepository;

  @BeforeEach
  void clean() {
    paymentPlansRepository.deleteAll();
    unitsRepository.deleteAll();
    projectsRepository.deleteAll();
    developersRepository.deleteAll();
    conversationsRepository.deleteAll();
  }

  private Developer developer(String name) {
    return developersService.create(new Developer().setName(name));
  }

  private Project project(Developer developer, String name) {
    return projectsService.create(developer.getId(), new Project().setName(name).setDeliveryMonths(36));
  }

  private Unit unit(Project project, String name, double price) {
    return unitsService.create(project.getId(), new Unit().setName(name).setPrice(price).setType(UnitType.RETAIL));
  }

  private static Payment payment(String name, int due, float percentage) {
    return new Payment().setName(name).setDue(due).setPercentage(percentage);
  }

  @Test
  void createGeneratesIdAndOneFindsIt() {
    final var created = developer("Sahara");

    assertThat(created.getId()).isNotBlank();
    assertThat(developersService.one(created.getId()).getName()).isEqualTo("Sahara");
    assertThat(developersService.find("missing")).isEmpty();
    assertThatThrownBy(() -> developersService.one("missing"))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("missing");
  }

  @Test
  void createWithParentLinksTheParent() {
    final var project = project(developer("Sahara"), "Mall");
    final var unit = unit(project, "F1-01", 1_000_000);
    final var plan = paymentPlansService.create(project.getId(), new PaymentPlan().setMonths(60));

    assertThat(ProjectDto.of(project).developerName()).isEqualTo("Sahara");
    assertThat(UnitDto.of(unit).projectName()).isEqualTo("Mall");
    assertThat(PaymentPlanDto.of(plan).projectId()).isEqualTo(project.getId());
  }

  @Test
  void createWithUnknownParentFailsAndSavesNothing() {
    assertThatThrownBy(() -> projectsService.create("missing", new Project().setName("Orphan")))
        .isInstanceOf(EntityNotFoundException.class);
    assertThat(projectsService.all(ProjectParams.builder().build())).isEmpty();
  }

  @Test
  void filtersByParentAndIds() {
    final var sahara = developer("Sahara");
    final var nileCrest = developer("Nile Crest");
    final var mall = project(sahara, "Mall");
    final var tower = project(nileCrest, "Tower");
    final var a = unit(mall, "A", 1);
    unit(tower, "B", 2);

    assertThat(projectsService.all(ProjectParams.builder().developerId(nileCrest.getId()).build()))
        .extracting(Project::getName).containsExactly("Tower");
    assertThat(unitsService.all(UnitParams.builder().projectId(mall.getId()).build()))
        .extracting(Unit::getId).containsExactly(a.getId());
    assertThat(developersService.all(DeveloperParams.builder().id(sahara.getId()).build()))
        .extracting(Developer::getName).containsExactly("Sahara");

    assertThat(projectsService.all(ProjectParams.builder().developerIds(List.of(sahara.getId(), nileCrest.getId())).build()))
        .extracting(Project::getName).containsExactlyInAnyOrder("Mall", "Tower");
    assertThat(unitsService.all(UnitParams.builder().projectIds(List.of(tower.getId())).build()))
        .extracting(Unit::getName).containsExactly("B");
  }

  @Test
  void paymentsWithTheSameDueAndPercentageButDifferentNamesAreBothKept() {
    final var plan = paymentPlansService.create(project(developer("Sahara"), "Mall").getId(), new PaymentPlan()
        .setMonths(60)
        .setPayments(new HashSet<>(List.of(payment("installment", 36, 10), payment("maintenance", 36, 10)))));

    assertThat(paymentPlansService.all(PaymentPlanParams.builder().id(plan.getId()).build()).getFirst().getPayments())
        .extracting(Payment::getName).containsExactlyInAnyOrder("installment", "maintenance");
  }

  @Test
  void sortsAndPages() {
    developer("C");
    developer("A");
    developer("B");

    final var firstPage = DeveloperParams.builder().page(0).count(2).sortField("name").sortDirection(Direction.ASC).build();
    final var secondPage = DeveloperParams.builder().page(1).count(2).sortField("name").sortDirection(Direction.ASC).build();
    final var descending = DeveloperParams.builder().sortField("name").sortDirection(Direction.DESC).build();

    assertThat(developersService.all(firstPage)).extracting(Developer::getName).containsExactly("A", "B");
    assertThat(developersService.all(secondPage)).extracting(Developer::getName).containsExactly("C");
    assertThat(developersService.all(descending)).extracting(Developer::getName).containsExactly("C", "B", "A");
  }

  @Test
  void listedEntitiesCanBeMappedOutsideATransaction() {
    final var project = project(developer("Sahara"), "Mall");
    unit(project, "F1-01", 1_000_000);
    paymentPlansService.create(project.getId(), new PaymentPlan().setMonths(60).setPayments(new HashSet<>(List.of(payment("downpayment", 0, 10)))));

    // lazy loading outside a transaction is disabled, so these only pass because the associations are fetched
    assertThat(projectsService.all(ProjectParams.builder().build())).map(ProjectDto::of)
        .extracting(ProjectDto::developerName).containsExactly("Sahara");
    assertThat(unitsService.all(UnitParams.builder().build())).map(UnitDto::of)
        .extracting(UnitDto::projectName).containsExactly("Mall");
    assertThat(paymentPlansService.all(PaymentPlanParams.builder().build())).map(PaymentPlanDto::of)
        .flatExtracting(PaymentPlanDto::payments).hasSize(1);
  }

  @Test
  void updateAppliesUnitPriceAndType() {
    final var unit = unit(project(developer("Sahara"), "Mall"), "F1-01", 1_000_000);

    unitsService.update(unit.getId(), new Unit().setPrice(1_200_000.0).setType(UnitType.OFFICE));

    final var updated = unitsService.one(unit.getId());
    assertThat(updated.getPrice()).isEqualTo(1_200_000.0);
    assertThat(updated.getType()).isEqualTo(UnitType.OFFICE);
    assertThat(updated.getName()).isEqualTo("F1-01");
  }

  @Test
  void updateAppliesPaymentPlanChanges() {
    final var project = project(developer("Sahara"), "Mall");
    final var plan = paymentPlansService.create(project.getId(), new PaymentPlan()
        .setMonths(60)
        .setPayments(new HashSet<>(List.of(payment("downpayment", 0, 10)))));

    paymentPlansService.update(plan.getId(), new PaymentPlan()
        .setMonths(84)
        .setDiscountPercentage(20.0)
        .setPayments(new HashSet<>(List.of(payment("downpayment", 0, 20), payment("maintenance", 36, 10)))));

    final var updated = paymentPlansService.all(PaymentPlanParams.builder().id(plan.getId()).build()).getFirst();
    assertThat(updated.getMonths()).isEqualTo(84);
    assertThat(updated.getDiscountPercentage()).isEqualTo(20.0);
    assertThat(updated.getPayments()).extracting(Payment::getName, Payment::getDue, Payment::getPercentage)
        .containsExactlyInAnyOrder(
            tuple("downpayment", 0, 20f),
            tuple("maintenance", 36, 10f));
  }

  @Test
  void assessesAUnitAgainstEveryPlanOfItsProject() {
    final var project = project(developer("Sahara"), "Mall");
    final var unit = unit(project, "F1-01", 1200);
    paymentPlansService.create(project.getId(), new PaymentPlan().setMonths(12).setPayments(new HashSet<>(List.of(payment("downpayment", 0, 10)))));
    paymentPlansService.create(project.getId(), new PaymentPlan().setMonths(24).setDiscountPercentage(10.0));

    // runs outside any transaction: the unit's project id comes from its lazy reference, the payments from the entity
    // graph
    final var results = cashBurnService.assessUnitCashBurn(new AssesUnitCashBurnRequestDto(unit.getId(), 100_000.0, 0.0, 1));

    assertThat(results).extracting(CashBurnResponseDto::effectiveUnitPrice).containsExactlyInAnyOrder(1200.0, 1080.0);
    assertThat(results).allSatisfy(result -> assertThat(result.totalUnpaidToDeveloper()).isLessThan(0.01));
  }

  @Test
  void deleteRemovesTheEntity() {
    final var developer = developer("Sahara");

    developersService.delete(developer.getId());

    assertThat(developersService.find(developer.getId())).isEmpty();
  }

  @Test
  void replaceMessagesUpsertsTheConversation() {
    final var message = new ConversationMessage().setType("user").setContent("hi").setTimestamp(Instant.now());
    final var reply = new ConversationMessage().setType("assistant").setContent("hello").setTimestamp(Instant.now());

    conversationsService.replaceMessages("c1", List.of(message));
    conversationsService.replaceMessages("c1", List.of(message, reply));

    assertThat(conversationsService.one("c1").getMessages())
        .extracting(ConversationMessage::getContent).containsExactly("hi", "hello");
    assertThat(conversationsService.ids()).containsExactly("c1");
  }

  @Test
  void toolsReturnAnErrorStringAndSaveNothingWhenTheParentIsMissing() {
    final var result = toolsService.createProject(new ProjectDto(null, "missing", null, "Mall", null, null, 36));

    assertThat(result).isEqualTo("ERROR: could not create project: Not found with ID: missing");
    assertThat(projectsService.all(ProjectParams.builder().build())).isEmpty();
  }

  @Test
  void toolsCreateAndListUnitsWithType() {
    final var project = project(developer("Sahara"), "Mall");

    toolsService.createUnit(new UnitDto(null, project.getId(), null, "G-02", 40.0, null, 0, 5_000_000.0, UnitType.RETAIL));

    assertThat(toolsService.listUnits(project.getId())).asInstanceOf(LIST)
        .singleElement()
        .satisfies(dto -> assertThat(((UnitDto) dto).type()).isEqualTo(UnitType.RETAIL));
    assertThat(toolsService.listDevelopers()).asInstanceOf(LIST)
        .containsExactly(new DeveloperDto(developersService.all(DeveloperParams.builder().build()).getFirst().getId(), "Sahara"));
  }

}
