package com.grahambartley.nightborn.raid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NoiseLevelsTest {

  private static final UUID SLEEPER = new UUID(1L, 2L);
  private static final UUID OTHER = new UUID(3L, 4L);

  @ParameterizedTest(name = "steps every {0} ticks, {1} each, {2} times: {3}")
  @CsvSource({
    "20, 0.5, 4, 0.5",
    "20, 2, 6, 7",
    "0, 2, 5, 10",
    "20, 5, 2, 9",
    "10, 5, 2, 9.5",
    "100, 4, 3, 4"
  })
  @DisplayName("a sleeper adds up the sounds it hears as each one fades a second at a time")
  void addsUp(final long gap, final double loudness, final int times, final double expected) {
    final NoiseLevels levels = new NoiseLevels();
    double level = 0.0;
    for (int heard = 0; heard < times; heard++) {
      level = levels.hear(SLEEPER, loudness, heard * gap, 1);
    }
    assertEquals(expected, level, 1e-9);
  }

  @Test
  @DisplayName("each sleeper keeps its own count")
  void keepsSleepersApart() {
    final NoiseLevels levels = new NoiseLevels();
    levels.hear(SLEEPER, 6.0, 0L, 1);
    levels.hear(OTHER, 2.0, 0L, 1);
    assertEquals(6.0, levels.level(SLEEPER, 0L, 1), 1e-9);
    assertEquals(2.0, levels.level(OTHER, 0L, 1), 1e-9);
  }

  @ParameterizedTest(name = "settled {0} ticks later keeps {1}")
  @CsvSource({"0, 2", "40, 1", "120, 0"})
  @DisplayName("sleepers whose noise has died away are let go")
  void settles(final long later, final int kept) {
    final NoiseLevels levels = new NoiseLevels();
    levels.hear(SLEEPER, 6.0, 0L, 1);
    levels.hear(OTHER, 2.0, 0L, 1);
    levels.settle(later, 1);
    assertEquals(kept, levels.size());
  }

  @Test
  @DisplayName("a woken sleeper starts from silence next time")
  void forgets() {
    final NoiseLevels levels = new NoiseLevels();
    levels.hear(SLEEPER, 9.0, 0L, 1);
    levels.forget(SLEEPER);
    assertEquals(2.0, levels.hear(SLEEPER, 2.0, 0L, 1), 1e-9);
  }
}
