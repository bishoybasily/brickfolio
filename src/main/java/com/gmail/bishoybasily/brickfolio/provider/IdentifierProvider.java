package com.gmail.bishoybasily.brickfolio.provider;

import java.util.UUID;

public interface IdentifierProvider {

  String generateId();

  static IdentifierProvider live() {
    return new IdentifierProvider() {
      @Override
      public String generateId() {
        return UUID.randomUUID().toString();
      }
    };
  }

}
