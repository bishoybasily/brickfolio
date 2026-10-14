package com.gmail.bishoybasily.brickfolio.controller;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.gmail.bishoybasily.brickfolio.domain.entity.Project;
import com.gmail.bishoybasily.brickfolio.domain.entity.Unit;
import com.gmail.bishoybasily.brickfolio.domain.parmas.ProjectParams;
import com.gmail.bishoybasily.brickfolio.domain.parmas.UnitParams;
import com.gmail.bishoybasily.brickfolio.service.jpa.ProjectsService;
import com.gmail.bishoybasily.brickfolio.service.jpa.UnitsService;

@Controller
@RequiredArgsConstructor
public class UnitController {

  private final UnitsService unitsService;
  private final ProjectsService projectsService;

  @QueryMapping
  public List<Unit> units() {
    return unitsService.all(UnitParams.builder().build());
  }

  @QueryMapping
  public Unit unit(@Argument String id) {
    return unitsService.one(id);
  }

  @BatchMapping
  public Map<Unit, Project> project(List<Unit> units) {
    final var ids = units.stream()
        .map(Unit::getProject)
        .filter(Objects::nonNull)
        .map(Project::getId)
        .distinct()
        .toList();

    final var projects = projectsService.all(ProjectParams.builder().ids(ids).build()).stream()
        .collect(toMap(Project::getId, identity()));

    return units.stream()
        .filter(unit -> unit.getProject() != null)
        .collect(toMap(identity(), unit -> projects.get(unit.getProject().getId())));
  }

}
