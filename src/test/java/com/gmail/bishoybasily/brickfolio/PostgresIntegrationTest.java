package com.gmail.bishoybasily.brickfolio;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

// One container for the whole run: Spring caches the context across test classes, so a per-class @Container would
// leave it pointing at a stopped database.
@SpringBootTest
public abstract class PostgresIntegrationTest {

  @ServiceConnection
  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16.4-alpine3.20");

  static {
    POSTGRES.start();
  }

}
