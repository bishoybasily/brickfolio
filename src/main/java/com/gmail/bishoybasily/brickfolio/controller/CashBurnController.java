package com.gmail.bishoybasily.brickfolio.controller;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.gmail.bishoybasily.brickfolio.domain.dto.AssesUnitCashBurnRequestDto;
import com.gmail.bishoybasily.brickfolio.domain.dto.CashBurnRequestDto;
import com.gmail.bishoybasily.brickfolio.domain.dto.CashBurnResponseDto;
import com.gmail.bishoybasily.brickfolio.service.CashBurnService;

@Controller
@RequiredArgsConstructor
public class CashBurnController {

  private final CashBurnService cashBurnService;

  @QueryMapping
  public CashBurnResponseDto simulateCashBurn(
      @Argument CashBurnRequestDto cashBurnRequestDto) {
    return cashBurnService.simulateCashBurn(cashBurnRequestDto);
  }

  @QueryMapping
  public List<CashBurnResponseDto> assessUnitSimulateCashBurn(
      @Argument AssesUnitCashBurnRequestDto assesUnitCashBurnRequestDto) {
    return cashBurnService.assessUnitCashBurn(assesUnitCashBurnRequestDto);
  }

}
