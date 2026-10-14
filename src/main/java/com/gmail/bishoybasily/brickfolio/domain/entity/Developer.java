package com.gmail.bishoybasily.brickfolio.domain.entity;

import static com.gmail.bishoybasily.brickfolio.constants.Constants.Entities.DEVELOPERS;
import static jakarta.persistence.FetchType.LAZY;
import static org.springframework.util.ObjectUtils.isEmpty;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Accessors(chain = true)
@Entity(name = DEVELOPERS)
public class Developer implements _Entity<Developer> {

  @Id
  private String id;
  private String name;
  @OneToMany(fetch = LAZY, mappedBy = "developer")
  private Set<Project> projects = new HashSet<>();

  @Override
  public Developer update(Developer developer) {
    final var res = this;

    if (!isEmpty(developer.getName()))
      res.setName(developer.getName());

    return res;
  }

}
