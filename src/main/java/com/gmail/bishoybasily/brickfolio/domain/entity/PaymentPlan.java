package com.gmail.bishoybasily.brickfolio.domain.entity;

import static com.gmail.bishoybasily.brickfolio.constants.Constants.Columns.PAYMENT_PLAN_ID;
import static com.gmail.bishoybasily.brickfolio.constants.Constants.Entities.PAYMENT_PLANS;
import static com.gmail.bishoybasily.brickfolio.constants.Constants.Tables.PAYMENT_PLAN_PAYMENTS;
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
@Entity(name = PAYMENT_PLANS)
public class PaymentPlan implements _Entity<PaymentPlan> {

  @Id
  private String id;
  private Integer months;
  private Double discountPercentage;
  @ManyToOne(fetch = LAZY)
  private Project project;
  @JoinTable(name = PAYMENT_PLAN_PAYMENTS, joinColumns = @JoinColumn(name = PAYMENT_PLAN_ID))
  @ElementCollection(fetch = LAZY)
  private Set<Payment> payments = new HashSet<>();

  @Override
  public PaymentPlan update(PaymentPlan paymentPlan) {
    final var res = this;

    if (!isEmpty(paymentPlan.getMonths()))
      res.setMonths(paymentPlan.getMonths());

    if (!isEmpty(paymentPlan.getDiscountPercentage()))
      res.setDiscountPercentage(paymentPlan.getDiscountPercentage());

    if (!isEmpty(paymentPlan.getPayments())) {
      res.getPayments().clear();
      res.getPayments().addAll(paymentPlan.getPayments());
    }

    return res;
  }

}
