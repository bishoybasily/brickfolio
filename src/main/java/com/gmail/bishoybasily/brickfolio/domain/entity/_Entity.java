package com.gmail.bishoybasily.brickfolio.domain.entity;

public interface _Entity<E extends _Entity<?>> {

  String getId();

  default E update(E e) {
    return (E) this;
  }

}
