package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.MobSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MobLevitationTest {

  @ParameterizedTest(name = "rise {0}s, land {1}s, levitating {2}, fighting {3}, roll {4}: {5}")
  @CsvSource({
    "30, 15, false, true, 0.9, true",
    "30, 15, true, true, 0.9, true",
    "30, 15, false, false, 0.001, true",
    "30, 15, false, false, 0.5, false",
    "30, 15, true, false, 0.001, false",
    "30, 15, true, false, 0.5, true",
    "0, 15, false, true, 0.0, false",
    "0, 15, false, false, 0.0, false",
    "0, 15, true, true, 0.001, false",
    "30, 0, true, false, 0.0, true",
    "0, 0, true, true, 0.0, true"
  })
  @DisplayName(
      "a vampire levitates in a fight and now and then about its home, as the server allows")
  void wants(
      final int riseSeconds,
      final int landSeconds,
      final boolean levitating,
      final boolean fighting,
      final float roll,
      final boolean wants) {
    final MobSettings settings =
        new MobSettings(100, 40, riseSeconds, landSeconds, 8, 14, 60, 50, 64, 40, 16, 24);
    assertEquals(wants, MobLevitation.wants(levitating, fighting, roll, settings));
  }

  @ParameterizedTest(name = "{0} seconds: {1} per tick")
  @CsvSource({"0, 0", "-1, 0", "1, 0.05", "30, 0.0016666667", "15, 0.0033333334"})
  @DisplayName("an average wait in seconds becomes a chance per tick, and 0 means never")
  void chance(final int seconds, final float chance) {
    assertEquals(chance, MobLevitation.chancePerTick(seconds), 1.0E-7F);
  }
}
