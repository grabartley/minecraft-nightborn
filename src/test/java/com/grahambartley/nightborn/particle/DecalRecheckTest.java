package com.grahambartley.nightborn.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DecalRecheckTest {

  @ParameterizedTest(name = "age {0}, phase {1}: {2}")
  @CsvSource({
    "0, 0, true",
    "1, 0, false",
    "5, 0, true",
    "3, 2, true",
    "8, 2, true",
    "4, 2, false",
    "600, 0, true"
  })
  @DisplayName("a decal checks its surface on one tick in every five, offset by its phase")
  void due(final int age, final int phase, final boolean due) {
    assertEquals(due, DecalRecheck.due(age, phase));
  }

  @ParameterizedTest(name = "phase {0}")
  @ValueSource(ints = {0, 1, 2, 3, 4})
  @DisplayName("every phase checks exactly once in any window of five ticks")
  void oncePerWindow(final int phase) {
    for (int start = 0; start < 20; start++) {
      final int from = start;
      assertEquals(
          1,
          IntStream.range(from, from + DecalRecheck.EVERY_TICKS)
              .filter(age -> DecalRecheck.due(age, phase))
              .count());
    }
  }

  @ParameterizedTest(name = "roll {0}: phase {1}")
  @CsvSource({"0, 0", "4, 4", "5, 0", "7, 2", "-1, 4"})
  @DisplayName("any random roll maps to a phase inside the window")
  void phase(final int roll, final int phase) {
    assertEquals(phase, DecalRecheck.phase(roll));
  }
}
