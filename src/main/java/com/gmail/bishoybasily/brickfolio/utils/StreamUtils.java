package com.gmail.bishoybasily.brickfolio.utils;

import static java.lang.Math.min;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SIZED;

import java.util.Spliterators;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtils {

  public static <A, B, R> Stream<R> zip(Stream<A> streamA,
      Stream<B> streamB,
      BiFunction<? super A, ? super B, R> function) {

    final var isParallel = streamA.isParallel() || streamB.isParallel();
    final var splitrA = streamA.spliterator();
    final var splitrB = streamB.spliterator();
    final var characteristics = splitrA.characteristics() & splitrB.characteristics() & (SIZED | ORDERED);
    final var itrA = Spliterators.iterator(splitrA);
    final var itrB = Spliterators.iterator(splitrB);
    final var sizeA = splitrA.estimateSize();
    final var sizeB = splitrB.estimateSize();
    final var minSize = min(sizeA, sizeB);

    final var spliterator = new AbstractSpliterator<R>(minSize, characteristics) {
      @Override
      public boolean tryAdvance(Consumer<? super R> action) {
        if (itrA.hasNext() && itrB.hasNext()) {
          action.accept(function.apply(itrA.next(), itrB.next()));
          return true;
        }
        return false;
      }
    };

    return StreamSupport.stream(spliterator, isParallel)
        .onClose(streamA::close).onClose(streamB::close);
  }
}
