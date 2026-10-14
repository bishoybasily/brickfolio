package com.gmail.bishoybasily.brickfolio.domain.repository;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.gmail.bishoybasily.brickfolio.domain.entity.Project;

public interface ProjectsRepository extends JpaRepository<Project, String>, JpaSpecificationExecutor<Project> {

  // fetched up front: callers map these to DTOs and GraphQL outside a transaction
  @Override
  @EntityGraph(attributePaths = {"developer"})
  List<Project> findAll(@Nullable Specification<Project> spec);

  @Override
  @EntityGraph(attributePaths = {"developer"})
  List<Project> findAll(@Nullable Specification<Project> spec, Sort sort);

  @Override
  @EntityGraph(attributePaths = {"developer"})
  Page<Project> findAll(@Nullable Specification<Project> spec, Pageable pageable);

}
