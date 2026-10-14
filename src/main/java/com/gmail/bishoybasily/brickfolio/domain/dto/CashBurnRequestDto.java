package com.gmail.bishoybasily.brickfolio.domain.dto;

import java.util.List;
import java.util.Objects;

public record CashBurnRequestDto(
    List<ScheduledPaymentPercentageDto> scheduledPayments,
    double unitPrice,
    int installmentIntervalMonths,
    double initialDiscountPercentage,
    int installmentMonths,
    double initialDeposit,
    double monthlyDeposit) {

  public Float getScheduledPaymentOn(Integer month, Float otherwise) {
    for (var scheduledPayment : scheduledPayments)
      if (Objects.equals(scheduledPayment.dueMonth(), month))
      return scheduledPayment.amountPercentage();
    return otherwise;
  }

}
