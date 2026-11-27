package com.grahambartley.nightborn.lore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import net.minecraft.util.math.random.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class LoreShelfTest {

  private static List<Integer> shelf(final int size) {
    return IntStream.range(0, size).boxed().toList();
  }

  @ParameterizedTest(name = "seed {0}")
  @ValueSource(longs = {0L, 1L, 42L, -7L, Long.MAX_VALUE})
  @DisplayName("an empty shelf gives no book")
  void anEmptyShelfGivesNothing(final long seed) {
    assertEquals(Optional.empty(), LoreShelf.pick(List.of(), Random.create(seed)));
  }

  @ParameterizedTest(name = "a shelf of {0}, seed {1}")
  @CsvSource({"1, 0", "1, 99", "2, 5", "7, 11", "9, -3", "20, 1234"})
  @DisplayName("a pick is always a book from the shelf")
  void aPickComesFromTheShelf(final int size, final long seed) {
    final List<Integer> shelf = shelf(size);
    final Random random = Random.create(seed);
    for (int draw = 0; draw < 50; draw++) {
      assertTrue(shelf.contains(LoreShelf.pick(shelf, random).orElseThrow()));
    }
  }

  @ParameterizedTest(name = "a shelf of {0}")
  @ValueSource(ints = {2, 7, 9, 20})
  @DisplayName("enough picks reach every book on the shelf")
  void everyBookCanBePicked(final int size) {
    final List<Integer> shelf = shelf(size);
    final Random random = Random.create(size);
    final Set<Integer> seen = new HashSet<>();
    for (int draw = 0; draw < size * 100; draw++) {
      seen.add(LoreShelf.pick(shelf, random).orElseThrow());
    }
    assertEquals(new HashSet<>(shelf), seen);
  }

  @ParameterizedTest(name = "seed {0}")
  @ValueSource(longs = {0L, 3L, 77L, -12L})
  @DisplayName("the same seed picks the same book")
  void theSameSeedPicksTheSameBook(final long seed) {
    final List<Integer> shelf = shelf(9);
    assertEquals(
        LoreShelf.pick(shelf, Random.create(seed)), LoreShelf.pick(shelf, Random.create(seed)));
  }

  @Test
  @DisplayName("two lecterns side by side do not always share a book")
  void neighbouringSeedsSpreadAcrossTheShelf() {
    final List<Integer> shelf = shelf(9);
    final Set<Integer> seen = new HashSet<>();
    for (long seed = 0; seed < 64; seed++) {
      seen.add(LoreShelf.pick(shelf, Random.create(seed)).orElseThrow());
    }
    assertTrue(seen.size() > 1);
  }
}
