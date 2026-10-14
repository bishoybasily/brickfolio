package com.gmail.bishoybasily.brickfolio.service.jpa;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.support.TransactionTemplate;

import com.gmail.bishoybasily.brickfolio.domain.entity._Entity;
import com.gmail.bishoybasily.brickfolio.domain.parmas.Params;

// Single repository calls are already transactional; only multi-step writes use the write template.
public interface _JpaService<E extends _Entity<E>, I, P extends Params<E>> {

  JpaRepository<E, I> getJpaRepository();

  JpaSpecificationExecutor<E> getJpaSpecificationExecutor();

  TransactionTemplate getWriteTransactionTemplate();

  default List<E> all(P p) {
    if (p.isPaginationPresented())
      return getJpaSpecificationExecutor().findAll(p.toSpecification(), p.pageable()).getContent();
    if (p.isSortPresented())
      return getJpaSpecificationExecutor().findAll(p.toSpecification(), p.sort());
    return getJpaSpecificationExecutor().findAll(p.toSpecification());
  }

  default Optional<E> find(I i) {
    return getJpaRepository().findById(i);
  }

  default E one(I i) {
    return getJpaRepository().findById(i).orElseThrow(() -> {
      return new EntityNotFoundException("Not found with ID: " + i);
    });
  }

  default E create(E e) {
    return getJpaRepository().save(e);
  }

  default E update(I i, E e) {
    return getWriteTransactionTemplate().execute(status -> {
      return getJpaRepository().save(one(i).update(e));
    });
  }

  default void delete(I i) {
    getJpaRepository().deleteById(i);
  }

}
