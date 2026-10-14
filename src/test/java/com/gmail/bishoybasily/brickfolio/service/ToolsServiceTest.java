package com.gmail.bishoybasily.brickfolio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;

import com.gmail.bishoybasily.brickfolio.domain.dto.DeveloperDto;
import com.gmail.bishoybasily.brickfolio.domain.dto.ProjectDto;
import com.gmail.bishoybasily.brickfolio.domain.entity.Project;
import com.gmail.bishoybasily.brickfolio.domain.parmas.UnitParams;
import com.gmail.bishoybasily.brickfolio.service.jpa.DevelopersService;
import com.gmail.bishoybasily.brickfolio.service.jpa.PaymentPlansService;
import com.gmail.bishoybasily.brickfolio.service.jpa.ProjectsService;
import com.gmail.bishoybasily.brickfolio.service.jpa.UnitsService;

class ToolsServiceTest {

  private final DevelopersService developersService = mock(DevelopersService.class);
  private final ProjectsService projectsService = mock(ProjectsService.class);
  private final UnitsService unitsService = mock(UnitsService.class);
  private final PaymentPlansService paymentPlansService = mock(PaymentPlansService.class);

  private final ToolsService tools = new ToolsService(developersService, projectsService, unitsService, paymentPlansService);

  @Test
  void returnsErrorStringOnValidationFailure() {
    final var result = tools.createDeveloper(new DeveloperDto(null, " "));

    assertThat(result).isEqualTo("ERROR: could not create developer: Developer name is required");
    verifyNoInteractions(developersService);
  }

  @Test
  void returnsErrorStringWhenTheServiceThrows() {
    when(projectsService.create(eq("missing"), any(Project.class)))
        .thenThrow(new EntityNotFoundException("Not found with ID: missing"));

    final var result = tools.createProject(new ProjectDto(null, "missing", null, "Mall", null, null, 36));

    assertThat(result).isEqualTo("ERROR: could not create project: Not found with ID: missing");
  }

  @Test
  void fallsBackToTheExceptionTypeWhenThereIsNoMessage() {
    when(unitsService.all(any(UnitParams.class))).thenThrow(new IllegalStateException());

    assertThat(tools.listUnits(null)).isEqualTo("ERROR: could not list units: IllegalStateException");
  }

}
