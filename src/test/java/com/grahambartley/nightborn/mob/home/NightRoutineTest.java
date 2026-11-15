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

  @ParameterizedTest(name = "bedtime {0}, decided {1}, night {2}, hungry {3}: decides {4}")
  @CsvSource({
    "false, 3, 4, true, true",
    "false, 4, 4, true, false",
    "true, 3, 4, true, false",
    "false, 3, 4, false, false",
    "false, -9223372036854775808, 0, true, true"
  })
  @DisplayName("a vampire weighs going out once a night, when it first wakes hungry")
  void decidesTonight(
      final boolean bedtime,
      final long decided,
      final long night,
      final boolean hungry,
      final boolean decides) {
    assertEquals(decides, NightRoutine.decidesTonight(bedtime, decided, night, hungry));
  }

  @ParameterizedTest(name = "roll {0} of {1}%: goes out {2}")
  @CsvSource({"10, 50, true", "49, 50, true", "50, 50, false", "0, 0, false", "99, 100, true"})
  @DisplayName("not every hungry vampire goes out every night")
  void goesOut(final int roll, final int chance, final boolean out) {
    assertEquals(out, NightRoutine.goesOut(roll, chance));
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

  @ParameterizedTest(name = "out {0}, feeding range {1}, home {2}: roams {3}")
  @CsvSource({
    "true, 64, 16, 64",
    "false, 64, 16, 16",
    "true, 8, 16, 16",
    "false, 256, 32, 32",
    "true, 20, 32, 32"
  })
  @DisplayName("a vampire roams its home, or its feeding range when out")
  void roamRadius(final boolean out, final int range, final int home, final int radius) {
    assertEquals(radius, NightRoutine.roamRadius(out, range, home));
  }

  @ParameterizedTest(
      name = "provoked {0}, out {1}, vampire {2} and target {3} blocks from home: out of range {4}")
  @CsvSource({
    "true, false, 40, 0, false",
    "true, false, 41, 0, true",
    "true, true, 60, 0, false",
    "true, true, 65, 0, true",
    "false, false, 200, 64, false",
    "false, true, 0, 65, true",
    "false, false, 0, 10, false"
  })
  @DisplayName(
      "prey must stay within the feeding range of home, and a chase ends past the chase range,"
          + " or the feeding range while out")
  void outOfRange(
      final boolean provoked,
      final boolean out,
      final double vampire,
      final double target,
      final boolean outOfRange) {
    assertEquals(
        outOfRange,
        NightRoutine.outOfRange(provoked, out, vampire * vampire, target * target, 40, 64));
  }

  @ParameterizedTest(name = "bedtime {0}, target {1}, downed {2}: ready {3}")
  @CsvSource({
    "true, false, false, true",
    "false, false, false, false",
    "true, true, false, false",
    "true, false, true, false"
  })
  @DisplayName("a vampire goes to bed at bedtime unless it is fighting or downed")
  void readyForBed(
      final boolean bedtime, final boolean target, final boolean downed, final boolean ready) {
    assertEquals(ready, NightRoutine.readyForBed(bedtime, target, downed));
  }

  @ParameterizedTest(name = "known {0}, {1} blocks away, home {2}, loaded {3}, free {4}: walks {5}")
  @CsvSource({
    "true, 10, 16, true, true, true",
    "true, 32, 16, true, true, true",
    "true, 33, 16, true, true, false",
    "false, 1, 16, true, true, false",
    "true, 1, 16, false, true, false",
    "true, 1, 16, true, false, false"
  })
  @DisplayName("a vampire walks to its own coffin when it is near, loaded and free")
  void walksToCoffin(
      final boolean known,
      final double distance,
      final int home,
      final boolean loaded,
      final boolean free,
      final boolean walks) {
    assertEquals(walks, NightRoutine.walksToCoffin(known, distance * distance, home, loaded, free));
  }
}
