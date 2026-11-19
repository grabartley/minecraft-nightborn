package com.grahambartley.nightborn.sun;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.LongStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ParasolWearTest {

  @ParameterizedTest(name = "lasting {0} s wears it out after {0} s of sun")
  @CsvSource({"180", "10", "60", "45", "1000", "3600"})
  @DisplayName("a parasol wears out after exactly its sun time")
  void wearsOutOnTime(final int seconds) {
    final long ticks = seconds * 20L;
    assertEquals(
        ParasolWear.DURABILITY - 1,
        LongStream.rangeClosed(1, ticks - 1).map(tick -> ParasolWear.points(tick, seconds)).sum());
    assertEquals(
        ParasolWear.DURABILITY,
        LongStream.rangeClosed(1, ticks).map(tick -> ParasolWear.points(tick, seconds)).sum());
  }

  @ParameterizedTest(name = "sun tick {0} at {1} s: {2} points")
  @CsvSource({"0, 180, 0", "-5, 180, 0", "1, 180, 0", "19, 180, 0", "20, 180, 1", "40, 180, 1"})
  @DisplayName("by default a parasol loses one point each second of sun")
  void oneASecondByDefault(final long tick, final int seconds, final int points) {
    assertEquals(points, ParasolWear.points(tick, seconds));
  }
}
