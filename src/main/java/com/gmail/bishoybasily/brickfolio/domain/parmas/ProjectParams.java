package com.gmail.bishoybasily.brickfolio.domain.parmas;

import static org.springframework.util.ObjectUtils.isEmpty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.gmail.bishoybasily.brickfolio.domain.entity.Project;

@Getter
@Setter
@ToString
@SuperBuilder
@Accessors(chain = true)
@NoArgsConstructor
public class ProjectParams extends Params<Project> {

  private String developerId;
  private List<String> developerIds;

  @Override
  public Specification<Project> toSpecification() {

    Specification<Project> spec = super.toSpecification();

    if (!isEmpty(developerId))
      spec = spec.and((root, query, criteriaBuilder) -> {
        return criteriaBuilder.equal(root.join("developer").get("id"), developerId);
      });

    if (!isEmpty(developerIds))
      spec = spec.and((root, query, criteriaBuilder) -> {
        return root.join("developer").get("id").in(developerIds);
      });

    return spec;
  }

}
