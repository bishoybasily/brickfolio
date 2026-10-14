package com.gmail.bishoybasily.brickfolio.service.jpa;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.gmail.bishoybasily.brickfolio.domain.entity.Developer;
import com.gmail.bishoybasily.brickfolio.domain.parmas.DeveloperParams;
import com.gmail.bishoybasily.brickfolio.domain.repository.DevelopersRepository;
import com.gmail.bishoybasily.brickfolio.provider.IdentifierProvider;

@Service
@RequiredArgsConstructor
public class DevelopersService implements _JpaService<Developer, String, DeveloperParams> {

  private final DevelopersRepository repository;

  private final IdentifierProvider identifierProvider;
  @Getter
  private final TransactionTemplate writeTransactionTemplate;

  @Override
  public Developer create(Developer developer) {
    return _JpaService.super.create(developer.setId(identifierProvider.generateId()));
  }

  @Override
  public JpaRepository<Developer, String> getJpaRepository() {
    return repository;
  }

  @Override
  public JpaSpecificationExecutor<Developer> getJpaSpecificationExecutor() {
    return repository;
  }

}
