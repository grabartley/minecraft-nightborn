package com.grahambartley.nightborn.name;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.IntUnaryOperator;

public final class NameDraw {

  private NameDraw() {}

  public static Optional<String> pick(
      final List<String> pool, final Collection<String> taken, final IntUnaryOperator below) {
    final List<String> free = new ArrayList<>(pool);
    free.removeAll(taken);
    final List<String> from = free.isEmpty() ? pool : free;
    return from.isEmpty() ? Optional.empty() : Optional.of(from.get(below.applyAsInt(from.size())));
  }
}
