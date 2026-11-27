package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.MobSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MobLevitationTest {

  @ParameterizedTest(
      name =
          "rise {0}s, land {1}s, levitating {2}, fighting {3}, settled {4}, room {5}, roll {6}:"
              + " {7}")
  @CsvSource({
    "30, 15, false, true, false, true, 0.9, true",
    "30, 15, true, true, false, true, 0.9, true",
    "30, 15, false, false, false, true, 0.001, true",
    "30, 15, false, false, true, true, 0.001, false",
    "30, 15, true, false, true, true, 0.5, false",
    "30, 15, true, true, true, true, 0.5, true",
    "30, 15, false, false, false, true, 0.5, false",
    "30, 15, true, false, false, true, 0.001, false",
    "30, 15, true, false, false, true, 0.5, true",
    "0, 15, false, true, false, true, 0.0, false",
    "0, 15, false, false, false, true, 0.0, false",
    "0, 15, true, true, false, true, 0.001, false",
    "30, 0, true, false, false, true, 0.0, true",
    "0, 0, true, true, false, true, 0.0, true",
    "30, 15, false, true, false, false, 0.9, false",
    "30, 15, true, true, false, false, 0.9, false",
    "30, 0, true, false, false, false, 0.0, false",
    "30, 15, false, false, false, false, 0.001, false"
  })
  @DisplayName(
      "a vampire levitates in a fight and now and then about its home, as the server allows, but"
          + " stays on its feet while it reads, sits or talks, or where there is no room to float")
  void wants(
      final int riseSeconds,
      final int landSeconds,
      final boolean levitating,
      final boolean fighting,
      final boolean settled,
      final boolean room,
      final float roll,
      final boolean wants) {
    final MobSettings settings =
        new MobSettings(100, 40, riseSeconds, landSeconds, 8, 14, 60, 50, 64, 40, 16, 24);
    assertEquals(wants, MobLevitation.wants(levitating, fighting, settled, room, roll, settings));
  }

  @ParameterizedTest(name = "{0} seconds: {1} per tick")
  @CsvSource({"0, 0", "-1, 0", "1, 0.05", "30, 0.0016666667", "15, 0.0033333334"})
  @DisplayName("an average wait in seconds becomes a chance per tick, and 0 means never")
  void chance(final int seconds, final float chance) {
    assertEquals(chance, MobLevitation.chancePerTick(seconds), 1.0E-7F);
  }
}
