package com.gmail.bishoybasily.brickfolio.domain.dto;

import java.util.List;

public record ScheduledPaymentValuesDto(Integer month, List<Double> amounts) {}
