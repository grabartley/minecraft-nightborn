package com.grahambartley.nightborn.mob.home;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NightRoutineTest {

  @ParameterizedTest(name = "time {0} is night {1}")
  @CsvSource({"13000, 0", "18000, 0", "36999, 0", "37000, 1", "12999, -1", "6000, -1", "61000, 2"})
  @DisplayName("each night is counted from its dusk, so one night spans midnight")
  void night(final long time, final long night) {
    assertEquals(night, NightRoutine.night(time));
  }

  @ParameterizedTest(name = "fixed {0}, time {1}: bedtime {2}")
  @CsvSource({
    "false, 6000, true",
    "false, 13000, false",
    "false, 21999, false",
    "false, 22000, true",
    "true, 6000, false"
  })
  @DisplayName("a vampire beds down by day and as the sun nears, never where there is no sun")
  void bedtime(final boolean fixed, final long time, final boolean bedtime) {
    assertEquals(bedtime, NightRoutine.bedtime(fixed, time));
  }

  @ParameterizedTest(name = "fixed {0}, time {1}, {2} blocks away at speed {3}: heads home {4}")
  @CsvSource({
    "false, 14000, 10, 0.3, false",
    "false, 21500, 10, 0.3, true",
    "false, 20000, 10, 0.3, false",
    "false, 20000, 64, 0.3, true",
    "false, 16000, 64, 0.3, false",
    "false, 16000, 64, 0.0, true",
    "false, 6000, 0, 0.3, true",
    "true, 21900, 64, 0.3, false"
  })
  @DisplayName("a vampire turns for home early enough to walk back before the sun nears")
  void headsHome(
      final boolean fixed,
      final long time,
      final double distance,
      final double speed,
      final boolean heads) {
    assertEquals(heads, NightRoutine.headsHome(fixed, time, distance, speed));
  }

  @ParameterizedTest(name = "hungry {0}, roll {1} of {2}%: goes out {3}")
  @CsvSource({
    "true, 10, 50, true",
    "true, 49, 50, true",
    "true, 50, 50, false",
    "false, 0, 100, false",
    "true, 0, 0, false",
    "true, 99, 100, true"
  })
  @DisplayName("only a hungry vampire goes out, and not every night")
  void goesOut(final boolean hungry, final int roll, final int chance, final boolean out) {
    assertEquals(out, NightRoutine.goesOut(hungry, roll, chance));
  }

  @ParameterizedTest(name = "outing night {0}, night {1}, hungry {2}, heading home {3}: out {4}")
  @CsvSource({
    "4, 4, true, false, true",
    "3, 4, true, false, false",
    "4, 4, false, false, false",
    "4, 4, true, true, false",
    "-9223372036854775808, -9223372036854775808, true, false, false"
  })
  @DisplayName("a vampire stays out only on the night it set out, while hungry and before dawn")
  void out(
      final long outing,
      final long night,
      final boolean hungry,
      final boolean headsHome,
      final boolean out) {
    assertEquals(out, NightRoutine.out(outing, night, hungry, headsHome));
  }

  @ParameterizedTest(name = "out {0}, feeding range {1}: roams {2}")
  @CsvSource({"true, 64, 64", "false, 64, 16", "true, 8, 16", "false, 256, 16"})
  @DisplayName("a vampire roams its home, or its feeding range when out")
  void roamRadius(final boolean out, final int range, final int radius) {
    assertEquals(radius, NightRoutine.roamRadius(out, range));
  }
}
