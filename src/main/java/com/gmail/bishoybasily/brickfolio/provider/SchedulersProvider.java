package com.gmail.bishoybasily.brickfolio.provider;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executor;

public interface SchedulersProvider {

  Scheduler ioScheduler();

  static SchedulersProvider from(Executor io) {
    final var ioScheduler = Schedulers.fromExecutor(io);
    return new SchedulersProvider() {
      @Override
      public Scheduler ioScheduler() {
        return ioScheduler;
      }
    };
  }

}
