package com.gmail.bishoybasily.brickfolio.service.jpa;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.gmail.bishoybasily.brickfolio.domain.entity.Project;
import com.gmail.bishoybasily.brickfolio.domain.parmas.ProjectParams;
import com.gmail.bishoybasily.brickfolio.domain.repository.ProjectsRepository;
import com.gmail.bishoybasily.brickfolio.provider.IdentifierProvider;

@Service
@RequiredArgsConstructor
public class ProjectsService implements _JpaService<Project, String, ProjectParams> {

  private final ProjectsRepository repository;

  private final DevelopersService developersService;
  private final IdentifierProvider identifierProvider;
  @Getter
  private final TransactionTemplate writeTransactionTemplate;

  @Override
  public Project create(Project project) {
    return _JpaService.super.create(project.setId(identifierProvider.generateId()));
  }

  public Project create(String developerId, Project project) {
    return writeTransactionTemplate.execute(status -> {
      return create(project.setDeveloper(developersService.one(developerId)));
    });
  }

  @Override
  public JpaRepository<Project, String> getJpaRepository() {
    return repository;
  }

  @Override
  public JpaSpecificationExecutor<Project> getJpaSpecificationExecutor() {
    return repository;
  }

}
