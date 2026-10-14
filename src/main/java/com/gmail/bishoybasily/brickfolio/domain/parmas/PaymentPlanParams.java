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

import com.gmail.bishoybasily.brickfolio.domain.entity.PaymentPlan;

@Getter
@Setter
@ToString
@SuperBuilder
@Accessors(chain = true)
@NoArgsConstructor
public class PaymentPlanParams extends Params<PaymentPlan> {

  private String projectId;
  private List<String> projectIds;

  @Override
  public Specification<PaymentPlan> toSpecification() {

    Specification<PaymentPlan> spec = super.toSpecification();

    if (!isEmpty(projectId))
      spec = spec.and((root, query, criteriaBuilder) -> {
        return criteriaBuilder.equal(root.join("project").get("id"), projectId);
      });

    if (!isEmpty(projectIds))
      spec = spec.and((root, query, criteriaBuilder) -> {
        return root.join("project").get("id").in(projectIds);
      });

    return spec;
  }

}
