package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HitLogTest {

  private static HitLog log() {
    final HitLog log = new HitLog();
    log.add(100L, 6.0F);
    log.add(110L, 4.0F);
    log.add(150L, 10.0F);
    return log;
  }

  @ParameterizedTest(name = "at time {0} with a window of {1}: {2} blows, {3} damage")
  @CsvSource({
    "150, 60, 3, 20",
    "159, 60, 3, 20",
    "160, 60, 2, 14",
    "170, 60, 1, 10",
    "209, 60, 1, 10",
    "210, 60, 0, 0",
    "150, 1, 1, 10",
    "150, 1000, 3, 20"
  })
  @DisplayName("the log counts only the blows inside the window")
  void window(final long now, final int window, final int hits, final float damage) {
    assertEquals(hits, log().hits(now, window));
    assertEquals(damage, log().damage(now, window), 1.0E-6F);
  }

  @Test
  @DisplayName("clearing the log forgets every blow")
  void clears() {
    final HitLog log = log();
    log.clear();
    assertEquals(0, log.hits(150L, 1000));
    assertEquals(0.0F, log.damage(150L, 1000));
  }
}
