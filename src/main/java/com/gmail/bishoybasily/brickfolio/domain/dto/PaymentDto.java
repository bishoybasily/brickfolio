package com.gmail.bishoybasily.brickfolio.domain.dto;

public record PaymentDto(
    String name,
    Float percentage,
    Integer due) {}
