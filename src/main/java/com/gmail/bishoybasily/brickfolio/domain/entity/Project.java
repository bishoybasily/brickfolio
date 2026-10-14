package com.gmail.bishoybasily.brickfolio.domain.entity;

import static com.gmail.bishoybasily.brickfolio.constants.Constants.Entities.PROJECTS;
import static jakarta.persistence.FetchType.LAZY;
import static org.springframework.util.ObjectUtils.isEmpty;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Accessors(chain = true)
@Entity(name = PROJECTS)
public class Project implements _Entity<Project> {

  @Id
  private String id;
  private String name;
  private String location;
  @Column(columnDefinition = "text")
  private String locationDescription;
  private Integer deliveryMonths;
  @ManyToOne(fetch = LAZY)
  private Developer developer;
  @OneToMany(fetch = LAZY, mappedBy = "project")
  private Set<Unit> units = new HashSet<>();
  @OneToMany(fetch = LAZY, mappedBy = "project")
  private Set<PaymentPlan> paymentPlans = new HashSet<>();

  @Override
  public Project update(Project project) {
    final var res = this;

    if (!isEmpty(project.getName()))
      res.setName(project.getName());

    if (!isEmpty(project.getLocation()))
      res.setLocation(project.getLocation());

    if (!isEmpty(project.getLocationDescription()))
      res.setLocationDescription(project.getLocationDescription());

    if (!isEmpty(project.getDeliveryMonths()))
      res.setDeliveryMonths(project.getDeliveryMonths());

    return res;
  }
}
