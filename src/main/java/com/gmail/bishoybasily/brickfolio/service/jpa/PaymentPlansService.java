package com.gmail.bishoybasily.brickfolio.service.jpa;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.gmail.bishoybasily.brickfolio.domain.entity.PaymentPlan;
import com.gmail.bishoybasily.brickfolio.domain.parmas.PaymentPlanParams;
import com.gmail.bishoybasily.brickfolio.domain.repository.PaymentPlansRepository;
import com.gmail.bishoybasily.brickfolio.provider.IdentifierProvider;

@Service
@RequiredArgsConstructor
public class PaymentPlansService implements _JpaService<PaymentPlan, String, PaymentPlanParams> {

  private final PaymentPlansRepository repository;

  private final ProjectsService projectsService;
  private final IdentifierProvider identifierProvider;
  @Getter
  private final TransactionTemplate writeTransactionTemplate;

  @Override
  public PaymentPlan create(PaymentPlan paymentPlan) {
    return _JpaService.super.create(paymentPlan.setId(identifierProvider.generateId()));
  }

  public PaymentPlan create(String projectId, PaymentPlan paymentPlan) {
    return writeTransactionTemplate.execute(status -> {
      return create(paymentPlan.setProject(projectsService.one(projectId)));
    });
  }

  @Override
  public JpaRepository<PaymentPlan, String> getJpaRepository() {
    return repository;
  }

  @Override
  public JpaSpecificationExecutor<PaymentPlan> getJpaSpecificationExecutor() {
    return repository;
  }
}
