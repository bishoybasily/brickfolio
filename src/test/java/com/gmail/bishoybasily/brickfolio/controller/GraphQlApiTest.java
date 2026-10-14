package com.gmail.bishoybasily.brickfolio.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.ExecutionGraphQlResponse;
import org.springframework.graphql.ExecutionGraphQlService;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.support.DefaultExecutionGraphQlRequest;

import com.gmail.bishoybasily.brickfolio.PostgresIntegrationTest;
import com.gmail.bishoybasily.brickfolio.domain.entity.*;
import com.gmail.bishoybasily.brickfolio.domain.repository.*;
import com.gmail.bishoybasily.brickfolio.service.jpa.DevelopersService;
import com.gmail.bishoybasily.brickfolio.service.jpa.PaymentPlansService;
import com.gmail.bishoybasily.brickfolio.service.jpa.ProjectsService;
import com.gmail.bishoybasily.brickfolio.service.jpa.UnitsService;

class GraphQlApiTest extends PostgresIntegrationTest {

  @Autowired
  ExecutionGraphQlService graphQlService;

  @Autowired
  DevelopersService developersService;
  @Autowired
  ProjectsService projectsService;
  @Autowired
  UnitsService unitsService;
  @Autowired
  PaymentPlansService paymentPlansService;

  @Autowired
  PaymentPlansRepository paymentPlansRepository;
  @Autowired
  UnitsRepository unitsRepository;
  @Autowired
  ProjectsRepository projectsRepository;
  @Autowired
  DevelopersRepository developersRepository;

  @BeforeEach
  void clean() {
    paymentPlansRepository.deleteAll();
    unitsRepository.deleteAll();
    projectsRepository.deleteAll();
    developersRepository.deleteAll();
  }

  private ExecutionGraphQlResponse execute(String document) {
    return graphQlService.execute(new DefaultExecutionGraphQlRequest(document, null, null, null, "1", null)).block();
  }

  @Test
  void missingEntityIsReportedAsNotFound() {
    final var response = execute("{ developer(id: \"missing\") { id } }");

    assertThat(response.getErrors()).singleElement().satisfies(error -> {
      assertThat(error.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
      assertThat(error.getMessage()).isEqualTo("Not found with ID: missing");
    });
  }

  @Test
  void resolvesEveryNestedFieldThroughTheBatchMappings() {
    final var developer = developersService.create(new Developer().setName("Sahara"));
    final var project = projectsService.create(developer.getId(), new Project().setName("Mall"));
    final var unit = unitsService.create(project.getId(), new Unit().setName("F1-01").setType(UnitType.RETAIL));
    paymentPlansService.create(project.getId(), new PaymentPlan()
        .setMonths(60)
        .setPayments(new HashSet<>(List.of(new Payment().setName("downpayment").setDue(0).setPercentage(10f)))));

    final var tree = execute("""
        { developers { name projects { name developer { name }
            units { name type project { name } }
            paymentPlans { months payments { name } } } } }
        """);

    assertThat(tree.getErrors()).isEmpty();
    assertThat(tree.field("developers[0].projects[0].developer.name").<String>getValue()).isEqualTo("Sahara");
    assertThat(tree.field("developers[0].projects[0].units[0].type").<String>getValue()).isEqualTo("RETAIL");
    assertThat(tree.field("developers[0].projects[0].units[0].project.name").<String>getValue()).isEqualTo("Mall");
    assertThat(tree.field("developers[0].projects[0].paymentPlans[0].payments[0].name").<String>getValue()).isEqualTo("downpayment");

    // unit(id) loads without the project; the batch mapping resolves it
    final var single = execute("{ unit(id: \"" + unit.getId() + "\") { project { name } } }");

    assertThat(single.getErrors()).isEmpty();
    assertThat(single.field("unit.project.name").<String>getValue()).isEqualTo("Mall");
  }

}
