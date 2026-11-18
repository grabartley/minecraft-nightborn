package com.grahambartley.nightborn.mind;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TitheRulesTest {

  @ParameterizedTest(name = "{0} damage, share {1}: kept {2}, passed {3}")
  @CsvSource({
    "10, 0.1, 9, 1",
    "10, 0.3, 7, 3",
    "10, 0.0, 10, 0",
    "10, 0.95, 2, 8",
    "10, -0.5, 10, 0"
  })
  @DisplayName("the tithe passes a share of each blow on, never more than the cap")
  void shares(final float amount, final double share, final float kept, final float passed) {
    assertEquals(kept, TitheRules.kept(amount, share), 1.0E-4);
    assertEquals(passed, TitheRules.passed(amount, share), 1.0E-4);
  }

  @ParameterizedTest(name = "{0} a minute: one blood after {1} seconds")
  @CsvSource({"2, 30", "3, 20", "6, 10"})
  @DisplayName("each rank pays a blood point every thirty, twenty, then ten seconds")
  void gathers(final double perMinute, final int seconds) {
    double progress = 0.0;
    int elapsed = 0;
    while (progress < 1.0 - 1.0E-9) {
      progress = TitheRules.gathered(progress, perMinute);
      elapsed++;
    }
    assertEquals(seconds, elapsed);
  }

  @ParameterizedTest(name = "{0} after the forms, {1} taken: share {2}")
  @CsvSource({"10, 7, 0.3", "10, 10, 0", "10, 12, 0", "0, 0, 0"})
  @DisplayName("when the cap lifts a blow back up, the bearer pays only what was really spared")
  void spared(final float formed, final float taken, final float share) {
    assertEquals(share, TitheRules.spared(formed, taken), 1.0E-6);
  }
}
