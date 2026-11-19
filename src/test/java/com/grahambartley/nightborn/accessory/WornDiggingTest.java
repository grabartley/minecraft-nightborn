package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WornDiggingTest {

  @ParameterizedTest(name = "speed {0}, soil {1}, x{2}: {3}")
  @CsvSource({
    "1.0, true, 1.2, 1.2",
    "4.0, true, 1.2, 4.8",
    "4.0, false, 1.2, 4.0",
    "4.0, true, 1.0, 4.0",
    "4.0, true, 0.5, 4.0"
  })
  @DisplayName("soil digs faster with the gift and nothing else changes")
  void speed(final float speed, final boolean soil, final double factor, final float dug) {
    assertEquals(dug, WornDigging.speed(speed, soil, factor), 1.0E-5);
  }

  @Test
  @DisplayName("the soil the gift speeds up is a block tag in data")
  void soilIsData() {
    assertEquals("nightborn:grave_soil", WornDigging.GRAVE_SOIL.id().toString());
  }
}
