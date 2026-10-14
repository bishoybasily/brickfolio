package com.gmail.bishoybasily.brickfolio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gmail.bishoybasily.brickfolio.domain.dto.AssesUnitCashBurnRequestDto;
import com.gmail.bishoybasily.brickfolio.domain.dto.CashBurnRequestDto;
import com.gmail.bishoybasily.brickfolio.domain.dto.CashBurnResponseDto;
import com.gmail.bishoybasily.brickfolio.domain.dto.ScheduledPaymentPercentageDto;
import com.gmail.bishoybasily.brickfolio.domain.parmas.PaymentPlanParams;
import com.gmail.bishoybasily.brickfolio.service.jpa.PaymentPlansService;
import com.gmail.bishoybasily.brickfolio.service.jpa.UnitsService;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashBurnService {

  private final UnitsService unitsService;
  private final PaymentPlansService paymentPlansService;

  // 15% yearly savings rate (IDB), applied monthly
  private double calculateMonthlyInterest(double balance) {
    return balance * 0.15 / 12;
  }

  public CashBurnResponseDto simulateCashBurn(CashBurnRequestDto dto) {

    if (dto.initialDiscountPercentage() < 0 || dto.initialDiscountPercentage() > 100)
      throw new IllegalArgumentException("Invalid discount percentage");

    final double effectiveUnitPrice =
        dto.unitPrice() * (1.0 - dto.initialDiscountPercentage() / 100.0);

    log.info("Effective unit price: {} after {}% discount (original: {})",
        effectiveUnitPrice,
        dto.initialDiscountPercentage(),
        dto.unitPrice());

    double balance = dto.initialDeposit();

    double totalPaidToDeveloper = 0.0;
    double totalUnpaidToDeveloper = effectiveUnitPrice;

    final var paidAmounts = new HashMap<Integer, List<Double>>();

    double totalScheduledPercent = dto.scheduledPayments()
        .stream()
        .mapToDouble(ScheduledPaymentPercentageDto::amountPercentage)
        .sum();

    if (totalScheduledPercent >= 100.0)
      throw new IllegalArgumentException("Scheduled percentages exceed 100%");

    dto.scheduledPayments().stream()
        .mapToInt(ScheduledPaymentPercentageDto::dueMonth)
        .filter(dueMonth -> dueMonth > dto.installmentMonths())
        .findFirst()
        .ifPresent(dueMonth -> {
          throw new IllegalArgumentException(
              "Scheduled payment due month " + dueMonth + " exceeds installmentMonths " + dto.installmentMonths());
        });

    double financedPercent = 100.0 - totalScheduledPercent;
    if (financedPercent < 0)
      financedPercent = 0;

    double financedAmount = effectiveUnitPrice * financedPercent / 100.0;

    double monthlyInstallment = financedAmount / dto.installmentMonths();

    log.info("Total scheduled payments {}% = {} and the remaining financed amount is {}, which equals to {} monthly over {} month equal installments.", totalScheduledPercent, effectiveUnitPrice - financedAmount, financedAmount, monthlyInstallment, dto.installmentMonths());

    double downPaymentPercentage = dto.getScheduledPaymentOn(0, 0.0f);
    if (downPaymentPercentage > 0.0) {

      double downPaymentAmount =
          effectiveUnitPrice * downPaymentPercentage / 100.0;

      balance -= downPaymentAmount;

      totalPaidToDeveloper += downPaymentAmount;
      totalUnpaidToDeveloper -= downPaymentAmount;

      paidAmounts.computeIfAbsent(0, k -> new ArrayList<>()).add(downPaymentAmount);

      log.info("Month 0 payment {}% = {}. Balance = {}", downPaymentPercentage, downPaymentAmount, balance);
    }

    int monthsSurvived = 0;
    final int installmentIntervalMonths = dto.installmentIntervalMonths();

    for (int month = 1; month <= dto.installmentMonths(); month++) {

      if (totalUnpaidToDeveloper <= 0.01)
        break;

      double minBalance = balance;

      double scheduledPercent = dto.getScheduledPaymentOn(month, 0.0f);
      if (scheduledPercent > 0.0) {

        double scheduledAmount = effectiveUnitPrice * scheduledPercent / 100.0;

        balance -= scheduledAmount;

        totalPaidToDeveloper += scheduledAmount;
        totalUnpaidToDeveloper -= scheduledAmount;

        minBalance = Math.min(minBalance, balance);

        paidAmounts.computeIfAbsent(month, k -> new ArrayList<>()).add(scheduledAmount);

        log.info("Scheduled {}% = {} at month {}. Balance = {}", scheduledPercent, scheduledAmount, month, balance);
      }

      if (month % installmentIntervalMonths == 0 && financedPercent > 0) {

        double installment = monthlyInstallment * installmentIntervalMonths;

        if (installment > totalUnpaidToDeveloper)
          installment = totalUnpaidToDeveloper;

        balance -= installment;

        totalPaidToDeveloper += installment;
        totalUnpaidToDeveloper -= installment;

        minBalance = Math.min(minBalance, balance);

        paidAmounts.computeIfAbsent(month, k -> new ArrayList<>()).add(installment);

        log.info("Installment {} at month {}. Balance = {}", installment, month, balance);
      }

      if (balance <= 0) {
        log.warn("Insufficient funds on month {}, Covered years: {} Balance = {}", month, month / 12.0, balance);
        break;
      }

      balance += calculateMonthlyInterest(minBalance);

      balance += dto.monthlyDeposit();

      monthsSurvived++;

    }

    return new CashBurnResponseDto(
        effectiveUnitPrice,
        monthlyInstallment,
        monthsSurvived,
        totalPaidToDeveloper,
        totalUnpaidToDeveloper,
        balance,
        paidAmounts);
  }

  public List<CashBurnResponseDto> assessUnitCashBurn(AssesUnitCashBurnRequestDto dto) {
    final var unit = unitsService.one(dto.unitId());
    final var plans = paymentPlansService.all(PaymentPlanParams.builder().projectId(unit.getProject().getId()).build());

    return plans.stream()
        .map(plan -> {

          final var unitPrice = unit.getPrice();
          final var planDiscountPercentage = plan.getDiscountPercentage();

          final var payments = plan.getPayments();

          final var scheduledPayments = payments.stream()
              .map(ScheduledPaymentPercentageDto::from)
              .toList();

          return new CashBurnRequestDto(
              scheduledPayments,
              unitPrice,
              dto.installmentIntervalMonths(),
              planDiscountPercentage == null ? 0.0 : planDiscountPercentage,
              plan.getMonths(),
              dto.initialDeposit(),
              dto.monthlyDeposit());
        })
        .map(this::simulateCashBurn)
        .toList();
  }

}
