package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NoticingTest {

  private static final double EPSILON = 1.0E-9;

  @ParameterizedTest(name = "hunter {0}, notice {1}, sight {2}: {3}")
  @CsvSource({
    "true, 1.0, 1.0, 1.0",
    "true, 0.7, 1.0, 0.7",
    "true, 1.0, 1.5, 1.5",
    "true, 0.7, 1.5, 1.05",
    "false, 0.7, 1.5, 1.0"
  })
  @DisplayName("Shroud and Beacon change only how far hunters see the wearer")
  void scale(final boolean hunter, final double notice, final double sight, final double scale) {
    assertEquals(scale, Noticing.scale(hunter, notice, sight), EPSILON);
  }

  @ParameterizedTest(name = "range {0} at {1}: {2}")
  @CsvSource({"8, 1.0, 8", "10, 0.7, 7", "8, 0.9, 7", "0, 0.5, 0"})
  @DisplayName("villagers notice a Shroud wearer feeding from less far")
  void villagerRange(final int range, final double notice, final int seen) {
    assertEquals(seen, Noticing.range(range, notice));
  }
}
