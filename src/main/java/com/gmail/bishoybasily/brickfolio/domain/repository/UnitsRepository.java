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

import com.gmail.bishoybasily.brickfolio.domain.entity.Unit;

public interface UnitsRepository extends JpaRepository<Unit, String>, JpaSpecificationExecutor<Unit> {

  // fetched up front: callers map these to DTOs and GraphQL outside a transaction
  @Override
  @EntityGraph(attributePaths = {"project"})
  List<Unit> findAll(@Nullable Specification<Unit> spec);

  @Override
  @EntityGraph(attributePaths = {"project"})
  List<Unit> findAll(@Nullable Specification<Unit> spec, Sort sort);

  @Override
  @EntityGraph(attributePaths = {"project"})
  Page<Unit> findAll(@Nullable Specification<Unit> spec, Pageable pageable);

}
