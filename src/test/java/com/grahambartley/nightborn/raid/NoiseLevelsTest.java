package com.grahambartley.nightborn.raid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
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

  @ParameterizedTest(name = "first {0}, second {1}")
  @CsvSource({"6, 2", "0.5, 9"})
  @DisplayName("each sleeper keeps its own count")
  void keepsSleepersApart(final double first, final double second) {
    final NoiseLevels levels = new NoiseLevels();
    levels.hear(SLEEPER, first, 0L, 1);
    levels.hear(OTHER, second, 0L, 1);
    assertEquals(first, levels.hear(SLEEPER, 0.0, 0L, 1), 1e-9);
    assertEquals(second, levels.hear(OTHER, 0.0, 0L, 1), 1e-9);
  }

  @ParameterizedTest(name = "settled {0} ticks later: {1} and {2} left")
  @CsvSource({"0, 6, 2", "40, 4, 0", "120, 0, 0"})
  @DisplayName("letting go of quiet sleepers never changes what a sleeper still hears")
  void settles(final long later, final double first, final double second) {
    final NoiseLevels levels = new NoiseLevels();
    levels.hear(SLEEPER, 6.0, 0L, 1);
    levels.hear(OTHER, 2.0, 0L, 1);
    levels.settle(later, 1);
    assertEquals(first, levels.hear(SLEEPER, 0.0, later, 1), 1e-9);
    assertEquals(second, levels.hear(OTHER, 0.0, later, 1), 1e-9);
  }

  @ParameterizedTest(name = "heard {0} then forgotten, next {1}")
  @CsvSource({"9, 2", "4, 6"})
  @DisplayName("a woken sleeper starts from silence next time")
  void forgets(final double before, final double next) {
    final NoiseLevels levels = new NoiseLevels();
    levels.hear(SLEEPER, before, 0L, 1);
    levels.forget(SLEEPER);
    assertEquals(next, levels.hear(SLEEPER, next, 0L, 1), 1e-9);
  }
}
