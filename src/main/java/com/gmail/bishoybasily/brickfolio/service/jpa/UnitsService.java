package com.gmail.bishoybasily.brickfolio.service.jpa;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.gmail.bishoybasily.brickfolio.domain.entity.Unit;
import com.gmail.bishoybasily.brickfolio.domain.parmas.UnitParams;
import com.gmail.bishoybasily.brickfolio.domain.repository.UnitsRepository;
import com.gmail.bishoybasily.brickfolio.provider.IdentifierProvider;

@Service
@RequiredArgsConstructor
public class UnitsService implements _JpaService<Unit, String, UnitParams> {

  private final UnitsRepository repository;

  private final ProjectsService projectsService;
  private final IdentifierProvider identifierProvider;
  @Getter
  private final TransactionTemplate writeTransactionTemplate;

  @Override
  public JpaRepository<Unit, String> getJpaRepository() {
    return repository;
  }

  @Override
  public JpaSpecificationExecutor<Unit> getJpaSpecificationExecutor() {
    return repository;
  }

  @Override
  public Unit create(Unit unit) {
    return _JpaService.super.create(unit.setId(identifierProvider.generateId()));
  }

  public Unit create(String projectId, Unit unit) {
    return writeTransactionTemplate.execute(status -> {
      return create(unit.setProject(projectsService.one(projectId)));
    });
  }

}
