package com.gmail.bishoybasily.brickfolio.domain.dto;

import com.gmail.bishoybasily.brickfolio.domain.entity.Project;

public record ProjectDto(
    String id,
    String developerId,
    String developerName,
    String name,
    String location,
    String locationDescription,
    Integer deliveryMonths) {

  public static ProjectDto of(Project project) {
    return new ProjectDto(project.getId(), project.getDeveloper().getId(), project.getDeveloper().getName(), project.getName(), project.getLocation(), project.getLocationDescription(), project.getDeliveryMonths());
  }

}
