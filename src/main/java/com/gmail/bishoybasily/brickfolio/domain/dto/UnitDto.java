package com.gmail.bishoybasily.brickfolio.domain.dto;

import com.gmail.bishoybasily.brickfolio.domain.entity.Unit;
import com.gmail.bishoybasily.brickfolio.domain.entity.UnitType;

public record UnitDto(
    String id,
    String projectId,
    String projectName,
    String name,
    Double indoorArea,
    Double outdoorArea,
    Integer floor,
    Double price,
    UnitType type) {

  public static UnitDto of(Unit unit) {
    return new UnitDto(unit.getId(), unit.getProject().getId(), unit.getProject().getName(), unit.getName(), unit.getIndoorArea(), unit.getOutdoorArea(), unit.getFloor(), unit.getPrice(), unit.getType());
  }

}
