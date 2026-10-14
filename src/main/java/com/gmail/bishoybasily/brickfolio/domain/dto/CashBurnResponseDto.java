package com.gmail.bishoybasily.brickfolio.domain.dto;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public record CashBurnResponseDto(
    double effectiveUnitPrice,
    double monthlyInstallment,
    int monthsSurvived,
    double totalPaidToDeveloper,
    double totalUnpaidToDeveloper,
    double remainingBalance,
    Map<Integer, List<Double>> amounts) {

  public List<ScheduledPaymentValuesDto> paidAmounts() {
    return amounts.entrySet().stream()
        .map(e -> new ScheduledPaymentValuesDto(e.getKey(), e.getValue()))
        .sorted(Comparator.comparing(ScheduledPaymentValuesDto::month))
        .toList();
  }
}
