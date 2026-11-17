package com.grahambartley.nightborn.age;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AgeNightTest {

  @ParameterizedTest(name = "time {0} -> night {1}")
  @CsvSource({"0, -1", "12999, -1", "13000, 0", "22999, 0", "23000, 0", "36999, 0", "37000, 1"})
  @DisplayName("each dusk starts a new night")
  void indexes(final long time, final long night) {
    assertEquals(night, AgeNight.index(time));
  }

  @ParameterizedTest(name = "night {0}, {1}..{2} -> {3}")
  @CsvSource({
    "0, 0, 13000, 0",
    "0, 13000, 13001, 1",
    "0, 12000, 24000, 10000",
    "0, 22000, 30000, 1000",
    "0, 23000, 30000, 0",
    "1, 13000, 23000, 0",
    "1, 37000, 37500, 500"
  })
  @DisplayName("only the dark part of a stretch of time is lived as night")
  void overlaps(final long night, final long from, final long to, final long ticks) {
    assertEquals(ticks, AgeNight.overlap(night, from, to));
  }

  @ParameterizedTest(name = "{0} ticks lived, {1}..{2} -> {3} ticks, {4} nights")
  @CsvSource({
    "0, 13000, 13001, 1, 0",
    "4999, 18000, 18001, 5000, 0",
    "5000, 18000, 18001, 5001, 1",
    "0, 13000, 24000, 10000, 1",
    "0, 20000, 24000, 3000, 0",
    "2500, 20000, 24000, 5500, 1",
    "0, 1000, 2000, 0, 0",
    "300, 18000, 18000, 300, 0",
    "300, 18000, 17000, 300, 0"
  })
  @DisplayName("a night counts once most of it is lived, and a skipped night counts whole")
  void counts(
      final int lived, final long previous, final long now, final int ticks, final int nights) {
    final AgeNight.Step step = AgeNight.step(new NightWatch(0, lived, false), previous, now);
    assertEquals(ticks, step.watch().ticks());
    assertEquals(nights, step.nightsLived());
    assertEquals(nights > 0, step.watch().counted());
  }

  @Test
  @DisplayName("a night that has counted does not count twice")
  void countsOnce() {
    final AgeNight.Step first = AgeNight.step(NightWatch.NONE, 13000, 19000);
    final AgeNight.Step second = AgeNight.step(first.watch(), 19000, 22999);
    assertEquals(1, first.nightsLived());
    assertEquals(0, second.nightsLived());
    assertTrue(second.watch().counted());
  }

  @Test
  @DisplayName("ticks from an earlier night do not carry into a later one")
  void doesNotCarryOver() {
    final AgeNight.Step later = AgeNight.step(new NightWatch(0, 4000, false), 37000, 41000);
    assertEquals(new NightWatch(1, 4000, false), later.watch());
    assertEquals(0, later.nightsLived());
  }

  @Test
  @DisplayName("a jump of many days counts no more than the nights of its last day")
  void capsLongJumps() {
    assertEquals(1, AgeNight.step(NightWatch.NONE, 0, 24000L * 50).nightsLived());
  }
}
