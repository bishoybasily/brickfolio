package com.gmail.bishoybasily.brickfolio.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

class PaymentTest {

  private static Payment payment(String name, int due, float percentage) {
    return new Payment().setName(name).setDue(due).setPercentage(percentage);
  }

  @Test
  void milestonesWithTheSameDueAndPercentageAreDistinctByName() {
    final var payments = new HashSet<>(List.of(payment("installment", 36, 10), payment("maintenance", 36, 10)));

    assertThat(payments).hasSize(2);
  }

  @Test
  void identicalMilestonesCollapse() {
    final var payments = new HashSet<>(List.of(payment("downpayment", 0, 10), payment("downpayment", 0, 10)));

    assertThat(payments).hasSize(1);
  }

}
