package com.gmail.bishoybasily.brickfolio.service;

import static org.springframework.util.ObjectUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import com.gmail.bishoybasily.brickfolio.domain.dto.DeveloperDto;
import com.gmail.bishoybasily.brickfolio.domain.dto.PaymentPlanDto;
import com.gmail.bishoybasily.brickfolio.domain.dto.ProjectDto;
import com.gmail.bishoybasily.brickfolio.domain.dto.UnitDto;
import com.gmail.bishoybasily.brickfolio.domain.entity.*;
import com.gmail.bishoybasily.brickfolio.domain.parmas.DeveloperParams;
import com.gmail.bishoybasily.brickfolio.domain.parmas.PaymentPlanParams;
import com.gmail.bishoybasily.brickfolio.domain.parmas.ProjectParams;
import com.gmail.bishoybasily.brickfolio.domain.parmas.UnitParams;
import com.gmail.bishoybasily.brickfolio.service.jpa.DevelopersService;
import com.gmail.bishoybasily.brickfolio.service.jpa.PaymentPlansService;
import com.gmail.bishoybasily.brickfolio.service.jpa.ProjectsService;
import com.gmail.bishoybasily.brickfolio.service.jpa.UnitsService;

// Failures go back to the model as an "ERROR: ..." string so it can recover within the conversation.
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolsService {

  private final DevelopersService developersService;
  private final ProjectsService projectsService;
  private final UnitsService unitsService;
  private final PaymentPlansService paymentPlansService;

  @Tool(description = "List all existing developers with their IDs and names. Use this to find matching developers before creating a new developer to avoid duplicates.")
  public Object listDevelopers() {
    return call("list developers", () -> {
      return developersService.all(DeveloperParams.builder().build()).stream().map(DeveloperDto::of).toList();
    });
  }

  @Tool(description = "Create a new developer. Returns the created developer with its ID and name.")
  public Object createDeveloper(@ToolParam(description = "Developer data to create (id should be null)") DeveloperDto dto) {
    return call("create developer", () -> {
      require(hasText(dto.name()), "Developer name is required");
      return DeveloperDto.of(developersService.create(new Developer().setName(dto.name())));
    });
  }

  @Tool(description = "List all existing projects with their IDs, names, and developer names. Use this to find matching projects before creating a new project to avoid duplicates.")
  public Object listProjects() {
    return call("list projects", () -> {
      return projectsService.all(ProjectParams.builder().build()).stream().map(ProjectDto::of).toList();
    });
  }

  @Tool(description = "Create a new project under an existing developer. Returns the created project with all its data including ID.")
  public Object createProject(@ToolParam(description = "Project data to create (id and developerName should be null)") ProjectDto dto) {
    return call("create project", () -> {
      require(hasText(dto.developerId()), "Developer ID is required");
      require(hasText(dto.name()), "Project name is required");
      return ProjectDto.of(projectsService.create(dto.developerId(), new Project()
          .setName(dto.name())
          .setLocation(dto.location())
          .setLocationDescription(dto.locationDescription())
          .setDeliveryMonths(dto.deliveryMonths())));
    });
  }

  @Tool(description = "List all units, optionally filtered by project ID. Use this to find matching units before creating a new unit.")
  public Object listUnits(@ToolParam(description = "Optional project ID to filter units", required = false) String projectId) {
    return call("list units", () -> {
      return unitsService.all(UnitParams.builder().projectId(projectId).build()).stream().map(UnitDto::of).toList();
    });
  }

  @Tool(description = "Create a new unit in an existing project. Returns the created unit with all its data including ID.")
  public Object createUnit(@ToolParam(description = "Unit data to create (id and projectName should be null)") UnitDto dto) {
    return call("create unit", () -> {
      require(hasText(dto.projectId()), "Project ID is required");
      return UnitDto.of(unitsService.create(dto.projectId(), new Unit()
          .setName(dto.name())
          .setIndoorArea(dto.indoorArea())
          .setOutdoorArea(dto.outdoorArea())
          .setFloor(dto.floor())
          .setPrice(dto.price())
          .setType(dto.type())));
    });
  }

  @Tool(description = "List all payment plans with their IDs and payments, optionally filtered by project ID. Use this to find matching payment plans before creating a new payment plan.")
  public Object listPaymentPlans(@ToolParam(description = "Optional project ID to filter payment plans", required = false) String projectId) {
    return call("list payment plans", () -> {
      return paymentPlansService.all(PaymentPlanParams.builder().projectId(projectId).build()).stream().map(PaymentPlanDto::of).toList();
    });
  }

  @Tool(description = "Create a new payment plan in an existing project. Returns the created payment plan with its ID and payments.")
  public Object createPaymentPlan(@ToolParam(description = "Payment plan data to create (id should be null)") PaymentPlanDto dto) {
    return call("create payment plan", () -> {
      require(hasText(dto.projectId()), "Project ID is required");

      final var paymentPlan = new PaymentPlan()
          .setDiscountPercentage(dto.discountPercentage())
          .setMonths(dto.months());

      if (dto.payments() != null)
        for (var paymentDto : dto.payments())
          paymentPlan.getPayments().add(
              new Payment().setDue(paymentDto.due()).setPercentage(paymentDto.percentage()).setName(paymentDto.name()));

      return PaymentPlanDto.of(paymentPlansService.create(dto.projectId(), paymentPlan));
    });
  }

  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalArgumentException(message);
  }

  private static Object call(String action, Supplier<?> supplier) {
    try {
      return supplier.get();
    } catch (RuntimeException e) {
      log.warn("Tool failed to {}", action, e);
      final var reason = isEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();
      return "ERROR: could not " + action + ": " + reason;
    }
  }

}
