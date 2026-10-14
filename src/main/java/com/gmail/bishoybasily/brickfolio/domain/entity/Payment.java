package com.gmail.bishoybasily.brickfolio.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Embeddable
@EqualsAndHashCode(of = {"name", "due", "percentage"})
public class Payment {

  private String name;
  private Integer due;
  private Float percentage;

}
