package com.gmail.bishoybasily.brickfolio.domain.entity;

import static com.gmail.bishoybasily.brickfolio.constants.Constants.Entities.UNITS;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static org.springframework.util.ObjectUtils.isEmpty;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Entity(name = UNITS)
public class Unit implements _Entity<Unit> {

  @Id
  private String id;
  private String name;
  private Double indoorArea;
  private Double outdoorArea;
  private Integer floor;
  private Double price;
  @Enumerated(STRING)
  private UnitType type;
  @ManyToOne(fetch = LAZY)
  private Project project;

  @Override
  public Unit update(Unit unit) {
    final var res = this;

    if (!isEmpty(unit.getName()))
      res.setName(unit.getName());

    if (!isEmpty(unit.getIndoorArea()))
      res.setIndoorArea(unit.getIndoorArea());

    if (!isEmpty(unit.getOutdoorArea()))
      res.setOutdoorArea(unit.getOutdoorArea());

    if (!isEmpty(unit.getFloor()))
      res.setFloor(unit.getFloor());

    if (!isEmpty(unit.getPrice()))
      res.setPrice(unit.getPrice());

    if (!isEmpty(unit.getType()))
      res.setType(unit.getType());

    return res;
  }
}
