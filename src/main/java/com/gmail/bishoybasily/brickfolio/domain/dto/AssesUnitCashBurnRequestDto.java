package com.gmail.bishoybasily.brickfolio.domain.dto;

public record AssesUnitCashBurnRequestDto(
    String unitId,
    Double initialDeposit,
    Double monthlyDeposit,
    int installmentIntervalMonths) {}
