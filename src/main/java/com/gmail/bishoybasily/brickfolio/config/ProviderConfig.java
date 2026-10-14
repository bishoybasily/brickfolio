package com.gmail.bishoybasily.brickfolio.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gmail.bishoybasily.brickfolio.provider.IdentifierProvider;
import com.gmail.bishoybasily.brickfolio.provider.SchedulersProvider;

@Configuration
public class ProviderConfig {

  @Bean
  SchedulersProvider schedulersProvider(Executor ioExecutor) {
    return SchedulersProvider.from(ioExecutor);
  }

  @Bean
  IdentifierProvider identifierProvider() {
    return IdentifierProvider.live();
  }

}
