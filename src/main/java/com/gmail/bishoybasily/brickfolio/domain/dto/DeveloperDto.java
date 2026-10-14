package com.gmail.bishoybasily.brickfolio.domain.dto;

import com.gmail.bishoybasily.brickfolio.domain.entity.Developer;

public record DeveloperDto(String id, String name) {

  public static DeveloperDto of(Developer developer) {
    return new DeveloperDto(developer.getId(), developer.getName());
  }

}
