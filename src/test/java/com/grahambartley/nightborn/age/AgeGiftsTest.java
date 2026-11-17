package com.grahambartley.nightborn.age;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.config.NightbornConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AgeGiftsTest {

  @ParameterizedTest(name = "rank {0} -> {1} health, {2} damage, {3} blood")
  @CsvSource({"-1, 0, 0, 0", "0, 0, 0, 0", "1, 4, 1, 2", "2, 8, 2, 4", "3, 12, 3, 6"})
  @DisplayName("each stage above fledgling adds the same gifts again")
  void gifts(final int rank, final double health, final double damage, final int blood) {
    assertEquals(
        new AgeGifts(health, damage, blood), AgeGifts.of(rank, NightbornConfig.DEFAULTS.age()));
  }

  @ParameterizedTest(name = "{0} s, rank {1}, {2}% -> {3} s")
  @CsvSource({
    "300, 0, 10, 300",
    "300, 1, 10, 333",
    "300, 2, 10, 375",
    "300, 3, 10, 428",
    "300, 3, 0, 300",
    "300, 3, 30, 3000",
    "300, 9, 30, 3000",
    "300, -2, 10, 300"
  })
  @DisplayName("blood drains slower with each stage and never stops")
  void drainsSlower(final int seconds, final int rank, final int percent, final int slowed) {
    assertEquals(slowed, AgeGifts.drainSeconds(seconds, rank, percent));
  }

  @ParameterizedTest(name = "{0} health under x{1} -> {2}")
  @CsvSource({"4, 1.0, 2", "12, 1.0, 6", "4, 0.0, 4", "4, -3.0, 4", "0, 1.0, 0"})
  @DisplayName("health is set so the vampire's doubled health gains exactly the gift")
  void healthAllowsForDoubling(final double health, final double multiplier, final double base) {
    assertEquals(base, new AgeGifts(health, 0, 0).healthBeforeMultiplier(multiplier));
  }

  @Test
  @DisplayName("a fledgling has no gifts")
  void fledglingHasNone() {
    final AgeGifts none = AgeGifts.of(0, NightbornConfig.DEFAULTS.age());
    assertTrue(none.health() == 0.0 && none.damage() == 0.0);
    assertFalse(none.bloodBar() > 0);
  }
}
