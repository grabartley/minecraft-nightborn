package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HouseholdSpotsTest {

  private static List<BlockPos> row(final int length) {
    return IntStream.range(0, length).mapToObj(x -> new BlockPos(x, 64, 0)).toList();
  }

  @ParameterizedTest(name = "{1} wanted from {0} spots gives {2}")
  @CsvSource({"10, 3, 3", "10, 0, 0", "2, 5, 2", "0, 4, 0", "6, 6, 6"})
  @DisplayName("never chooses more spots than were asked for or exist")
  void counts(final int spots, final int wanted, final int chosen) {
    assertEquals(chosen, HouseholdSpots.choose(row(spots), wanted, Random.create(1L)).size());
  }

  @ParameterizedTest(name = "seed {0}")
  @CsvSource({"1", "2", "3", "42", "1000"})
  @DisplayName("never puts two residents in the same spot")
  void distinct(final long seed) {
    final List<BlockPos> chosen = HouseholdSpots.choose(row(4), 4, Random.create(seed));
    assertEquals(chosen.size(), new HashSet<>(chosen).size());
  }

  @ParameterizedTest(name = "seed {0}")
  @CsvSource({"1", "2", "3", "42", "1000"})
  @DisplayName("keeps residents apart while there is room to")
  void spreadsOut(final long seed) {
    final List<BlockPos> chosen = HouseholdSpots.choose(row(12), 3, Random.create(seed));
    for (final BlockPos a : chosen) {
      for (final BlockPos b : chosen) {
        assertTrue(a == b || a.getManhattanDistance(b) >= 2, a + " crowds " + b);
      }
    }
  }

  @ParameterizedTest(name = "seed {0}")
  @CsvSource({"7", "8", "9"})
  @DisplayName("the same seed gives the same household")
  void repeatable(final long seed) {
    assertEquals(
        HouseholdSpots.choose(row(12), 4, Random.create(seed)),
        HouseholdSpots.choose(row(12), 4, Random.create(seed)));
  }
}
