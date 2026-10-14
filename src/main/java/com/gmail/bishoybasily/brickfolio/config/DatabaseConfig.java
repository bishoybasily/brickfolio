package com.gmail.bishoybasily.brickfolio.config;

import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class DatabaseConfig {

  @Bean
  TransactionTemplate readTransactionTemplate(PlatformTransactionManager txManager) {
    return new TransactionTemplate(txManager) {
      {
        setReadOnly(true);
        setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
      }
    };
  }

  @Bean
  TransactionTemplate writeTransactionTemplate(PlatformTransactionManager txManager) {
    return new TransactionTemplate(txManager) {
      {
        setReadOnly(false);
        setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
      }
    };
  }
}
