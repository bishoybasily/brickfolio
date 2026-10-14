package com.gmail.bishoybasily.brickfolio.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConcurrencyConfig {

  // Defining an Executor stops Boot creating applicationTaskExecutor, which Spring GraphQL runs blocking resolvers on;
  // the alias makes it use this virtual-thread executor instead of the event loop.
  @Bean({"ioExecutor", "applicationTaskExecutor"})
  Executor ioExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }

}
