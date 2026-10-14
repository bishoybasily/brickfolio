package com.gmail.bishoybasily.brickfolio.controller;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.gmail.bishoybasily.brickfolio.domain.entity.Developer;
import com.gmail.bishoybasily.brickfolio.domain.entity.Payment;
import com.gmail.bishoybasily.brickfolio.domain.entity.PaymentPlan;
import com.gmail.bishoybasily.brickfolio.domain.entity.Project;
import com.gmail.bishoybasily.brickfolio.domain.entity.Unit;
import com.gmail.bishoybasily.brickfolio.domain.parmas.DeveloperParams;
import com.gmail.bishoybasily.brickfolio.domain.parmas.PaymentPlanParams;
import com.gmail.bishoybasily.brickfolio.domain.parmas.ProjectParams;
import com.gmail.bishoybasily.brickfolio.domain.parmas.UnitParams;
import com.gmail.bishoybasily.brickfolio.service.jpa.DevelopersService;
import com.gmail.bishoybasily.brickfolio.service.jpa.PaymentPlansService;
import com.gmail.bishoybasily.brickfolio.service.jpa.ProjectsService;
import com.gmail.bishoybasily.brickfolio.service.jpa.UnitsService;

@Controller
@RequiredArgsConstructor
public class ProjectController {

  private final ProjectsService projectsService;
  private final DevelopersService developersService;
  private final UnitsService unitsService;
  private final PaymentPlansService paymentPlansService;

  @QueryMapping
  public List<Project> projects() {
    return projectsService.all(ProjectParams.builder().build());
  }

  @QueryMapping
  public Project project(@Argument String id) {
    return projectsService.one(id);
  }

  @BatchMapping
  public Map<Project, List<Unit>> units(List<Project> projects) {
    final var ids = projects.stream().map(Project::getId).toList();

    final var units = unitsService.all(UnitParams.builder().projectIds(ids).build()).stream()
        .collect(groupingBy(unit -> unit.getProject().getId()));

    return projects.stream()
        .collect(toMap(identity(), project -> units.getOrDefault(project.getId(), List.of())));
  }

  @BatchMapping
  public Map<Project, List<PaymentPlan>> paymentPlans(List<Project> projects) {
    final var ids = projects.stream().map(Project::getId).toList();

    final var paymentPlans = paymentPlansService.all(PaymentPlanParams.builder().projectIds(ids).build()).stream()
        .collect(groupingBy(paymentPlan -> paymentPlan.getProject().getId()));

    return projects.stream()
        .collect(toMap(identity(), project -> paymentPlans.getOrDefault(project.getId(), List.of())));
  }

  @BatchMapping
  public Map<Project, Developer> developer(List<Project> projects) {
    final var ids = projects.stream()
        .map(Project::getDeveloper)
        .filter(Objects::nonNull)
        .map(Developer::getId)
        .distinct()
        .toList();

    final var developers = developersService.all(DeveloperParams.builder().ids(ids).build()).stream()
        .collect(toMap(Developer::getId, identity()));

    return projects.stream()
        .filter(project -> project.getDeveloper() != null)
        .collect(toMap(identity(), project -> developers.get(project.getDeveloper().getId())));
  }

  @BatchMapping(typeName = "PaymentPlan")
  public Map<PaymentPlan, Set<Payment>> payments(List<PaymentPlan> paymentPlans) {
    final var ids = paymentPlans.stream().map(PaymentPlan::getId).toList();

    final var payments = paymentPlansService.all(PaymentPlanParams.builder().ids(ids).build()).stream()
        .collect(toMap(PaymentPlan::getId, PaymentPlan::getPayments));

    return paymentPlans.stream()
        .collect(toMap(identity(), paymentPlan -> payments.getOrDefault(paymentPlan.getId(), Set.of())));
  }

}
