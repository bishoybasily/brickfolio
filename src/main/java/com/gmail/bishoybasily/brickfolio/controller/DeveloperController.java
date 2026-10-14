package com.gmail.bishoybasily.brickfolio.controller;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.gmail.bishoybasily.brickfolio.domain.entity.Developer;
import com.gmail.bishoybasily.brickfolio.domain.entity.Project;
import com.gmail.bishoybasily.brickfolio.domain.parmas.DeveloperParams;
import com.gmail.bishoybasily.brickfolio.domain.parmas.ProjectParams;
import com.gmail.bishoybasily.brickfolio.service.jpa.DevelopersService;
import com.gmail.bishoybasily.brickfolio.service.jpa.ProjectsService;

@Controller
@RequiredArgsConstructor
public class DeveloperController {

  private final DevelopersService developersService;
  private final ProjectsService projectsService;

  @QueryMapping
  public List<Developer> developers() {
    return developersService.all(DeveloperParams.builder().build());
  }

  @QueryMapping
  public Developer developer(@Argument String id) {
    return developersService.one(id);
  }

  @BatchMapping
  public Map<Developer, List<Project>> projects(List<Developer> developers) {
    final var ids = developers.stream().map(Developer::getId).toList();

    final var projects = projectsService.all(ProjectParams.builder().developerIds(ids).build()).stream()
        .collect(groupingBy(project -> project.getDeveloper().getId()));

    return developers.stream()
        .collect(toMap(identity(), developer -> projects.getOrDefault(developer.getId(), List.of())));
  }

}
