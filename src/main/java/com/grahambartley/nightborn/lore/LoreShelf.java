package com.grahambartley.nightborn.lore;

import java.util.List;
import java.util.Optional;
import net.minecraft.util.math.random.Random;

public final class LoreShelf {

  private LoreShelf() {}

  public static <T> Optional<T> pick(final List<T> books, final Random random) {
    return books.isEmpty()
        ? Optional.empty()
        : Optional.of(books.get(random.nextInt(books.size())));
  }
}
