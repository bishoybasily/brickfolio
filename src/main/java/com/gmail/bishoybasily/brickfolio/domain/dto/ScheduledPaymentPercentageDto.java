package com.gmail.bishoybasily.brickfolio.domain.dto;

import com.gmail.bishoybasily.brickfolio.domain.entity.Payment;

public record ScheduledPaymentPercentageDto(Integer dueMonth, Float amountPercentage) {
  public static ScheduledPaymentPercentageDto from(Payment payment) {
    return new ScheduledPaymentPercentageDto(payment.getDue(), payment.getPercentage());
  }
}
