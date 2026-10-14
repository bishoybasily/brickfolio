package com.gmail.bishoybasily.brickfolio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.gmail.bishoybasily.brickfolio.domain.dto.CashBurnRequestDto;
import com.gmail.bishoybasily.brickfolio.domain.dto.ScheduledPaymentPercentageDto;

class CashBurnServiceTest {

  // simulateCashBurn is pure: it never touches the repositories
  private final CashBurnService service = new CashBurnService(null, null);

  private static ScheduledPaymentPercentageDto scheduled(int dueMonth, float percentage) {
    return new ScheduledPaymentPercentageDto(dueMonth, percentage);
  }

  @Test
  void singleInstallmentEarnsMonthlyInterestOnTheRemainingBalance() {
    final var result = service.simulateCashBurn(new CashBurnRequestDto(List.of(), 1200, 1, 0, 1, 2000, 0));

    assertThat(result.effectiveUnitPrice()).isEqualTo(1200);
    assertThat(result.monthlyInstallment()).isEqualTo(1200);
    assertThat(result.monthsSurvived()).isEqualTo(1);
    assertThat(result.totalPaidToDeveloper()).isEqualTo(1200);
    assertThat(result.totalUnpaidToDeveloper()).isEqualTo(0);
    // (2000 - 1200) + 800 * 15% / 12
    assertThat(result.remainingBalance()).isCloseTo(810, within(1e-9));
    assertThat(result.amounts()).isEqualTo(Map.of(1, List.of(1200.0)));
  }

  @Test
  void initialDiscountReducesTheEffectivePrice() {
    final var result = service.simulateCashBurn(new CashBurnRequestDto(List.of(), 1000, 1, 10, 10, 100_000, 0));

    assertThat(result.effectiveUnitPrice()).isCloseTo(900, within(1e-9));
    assertThat(result.monthlyInstallment()).isCloseTo(90, within(1e-9));
    assertThat(result.totalPaidToDeveloper()).isCloseTo(900, within(1e-9));
  }

  @Test
  void scheduledPaymentsAreTakenOutOfTheFinancedPartAndInstallmentsFollowTheInterval() {
    // 25% down, 25% at month 3, the remaining 50% (600) financed over 6 months, paid every 3 months
    final var request = new CashBurnRequestDto(List.of(scheduled(0, 25), scheduled(3, 25)), 1200, 3, 0, 6, 10_000, 100);

    final var result = service.simulateCashBurn(request);

    assertThat(result.monthlyInstallment()).isCloseTo(100, within(1e-9));
    assertThat(result.monthsSurvived()).isEqualTo(6);
    assertThat(result.totalPaidToDeveloper()).isCloseTo(1200, within(1e-9));
    assertThat(result.totalUnpaidToDeveloper()).isCloseTo(0, within(1e-9));
    assertThat(result.amounts()).isEqualTo(Map.of(
        0, List.of(300.0),
        3, List.of(300.0, 300.0),
        6, List.of(300.0)));
    // computed by hand: each month adds 1.25% interest on the lowest balance plus the 100 deposit
    assertThat(result.remainingBalance()).isCloseTo(10135.365093512344, within(1e-6));
  }

  @Test
  void lastMonthSettlesTheInstallmentsTheIntervalLeftOver() {
    // 1000 financed over 5 months paid every 3 months: 3 months at month 3, the remaining 2 at month 5
    final var result = service.simulateCashBurn(new CashBurnRequestDto(List.of(), 1000, 3, 0, 5, 100_000, 0));

    assertThat(result.monthsSurvived()).isEqualTo(5);
    assertThat(result.totalPaidToDeveloper()).isCloseTo(1000, within(1e-9));
    assertThat(result.totalUnpaidToDeveloper()).isCloseTo(0, within(1e-9));
    assertThat(result.amounts()).isEqualTo(Map.of(
        3, List.of(600.0),
        5, List.of(400.0)));
  }

  @Test
  void stopsWhenTheBalanceRunsOut() {
    final var result = service.simulateCashBurn(new CashBurnRequestDto(List.of(), 1000, 1, 0, 2, 100, 0));

    assertThat(result.monthsSurvived()).isEqualTo(0);
    assertThat(result.totalPaidToDeveloper()).isEqualTo(500);
    assertThat(result.totalUnpaidToDeveloper()).isEqualTo(500);
    assertThat(result.remainingBalance()).isEqualTo(-400);
  }

  @Test
  void rejectsInvalidDiscount() {
    assertThatThrownBy(() -> service.simulateCashBurn(new CashBurnRequestDto(List.of(), 1000, 1, 101, 12, 0, 0)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("discount");
  }

  @Test
  void rejectsInstallmentIntervalBelowOneMonth() {
    assertThatThrownBy(() -> service.simulateCashBurn(new CashBurnRequestDto(List.of(), 1000, 0, 0, 12, 0, 0)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("interval");
  }

  @Test
  void rejectsScheduledPaymentsOfOneHundredPercentOrMore() {
    final var request = new CashBurnRequestDto(List.of(scheduled(0, 60), scheduled(1, 40)), 1000, 1, 0, 12, 0, 0);

    assertThatThrownBy(() -> service.simulateCashBurn(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("exceed 100%");
  }

  @Test
  void rejectsScheduledPaymentsAfterTheLastInstallment() {
    final var request = new CashBurnRequestDto(List.of(scheduled(13, 10)), 1000, 1, 0, 12, 0, 0);

    assertThatThrownBy(() -> service.simulateCashBurn(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("exceeds installmentMonths");
  }

}
