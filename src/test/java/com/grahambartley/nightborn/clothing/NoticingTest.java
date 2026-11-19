package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NoticingTest {

  private static final double EPSILON = 1.0E-9;

  @ParameterizedTest(name = "notice {0}, sight {1}: {2}")
  @CsvSource({"1.0, 1.0, 1.0", "0.7, 1.0, 0.7", "1.0, 1.5, 1.5", "0.7, 1.5, 1.05", "0.2, 1.5, 0.3"})
  @DisplayName("Shroud and Beacon together set how far hunters see the wearer")
  void byHunter(final double notice, final double sight, final double scale) {
    assertEquals(scale, Noticing.byHunter(notice, sight), EPSILON);
  }

  @ParameterizedTest(name = "range {0} at {1}: {2}")
  @CsvSource({"8, 1.0, 8", "10, 0.7, 7", "8, 0.9, 7", "0, 0.5, 0"})
  @DisplayName("villagers notice a Shroud wearer feeding from less far")
  void villagerRange(final int range, final double notice, final int seen) {
    assertEquals(seen, Noticing.range(range, notice));
  }

  @ParameterizedTest(name = "range {0}, notice {1}, feeding {2}: {3}")
  @CsvSource({"16, 1.0, 1.0, 16", "16, 1.0, 0.7, 11", "16, 0.5, 0.7, 6", "10, 1.0, 0.7, 7"})
  @DisplayName("a feeding gift shortens how far a feeding is noticed, on top of worn noticing")
  void feeding(
      final int range, final double notice, final double feedingNotice, final int expected) {
    assertEquals(expected, Noticing.feeding(range, notice, feedingNotice));
  }
}
