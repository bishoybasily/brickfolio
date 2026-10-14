package com.gmail.bishoybasily.brickfolio.domain.dto;

import java.util.List;

import com.gmail.bishoybasily.brickfolio.domain.entity.PaymentPlan;

public record PaymentPlanDto(
    String id,
    String projectId,
    Integer months,
    Double discountPercentage,
    List<PaymentDto> payments) {

  public static PaymentPlanDto of(PaymentPlan paymentPlan) {
    return new PaymentPlanDto(paymentPlan.getId(), paymentPlan.getProject().getId(), paymentPlan.getMonths(), paymentPlan.getDiscountPercentage(), paymentPlan.getPayments().stream().map(payment -> new PaymentDto(payment.getName(), payment.getPercentage(), payment.getDue())).toList());
  }

}
