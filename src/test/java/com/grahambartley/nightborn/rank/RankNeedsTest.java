package com.grahambartley.nightborn.rank;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.OptionalInt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RankNeedsTest {

  @ParameterizedTest(name = "rank {0} -> {1}")
  @CsvSource({"0, 0", "1, 5", "2, 20", "3, 45", "4, -1", "-1, -1"})
  @DisplayName(
      "Initiate needs nothing, each rank above reads its setting, and past the top is shut")
  void pointsFor(final int rank, final int points) {
    final OptionalInt expected = points < 0 ? OptionalInt.empty() : OptionalInt.of(points);
    assertEquals(expected, RankFixtures.NEEDS.pointsFor(rank));
  }

  @ParameterizedTest(name = "{0} stored -> {1}")
  @CsvSource({"-4, 0", "0, 0", "7, 7"})
  @DisplayName("a threshold is never below zero")
  void neverNegative(final int stored, final int read) {
    assertEquals(OptionalInt.of(read), new RankNeeds(List.of(stored)).pointsFor(1));
  }
}
