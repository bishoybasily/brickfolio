package com.gmail.bishoybasily.brickfolio.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

class SchedulersProviderTest {

  @Test
  void ioSchedulerIsCreatedOnce() {
    try (final var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      final var provider = SchedulersProvider.from(executor);

      assertThat(provider.ioScheduler()).isSameAs(provider.ioScheduler());
    }
  }

}
