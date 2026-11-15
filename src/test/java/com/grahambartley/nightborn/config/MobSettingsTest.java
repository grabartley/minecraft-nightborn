package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class MobSettingsTest {

  static Stream<Setting<?>> settings() {
    return MobSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(MobSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @ParameterizedTest(name = "{0} is kept as {1}")
  @CsvSource({"-20, 0", "0, 0", "55, 55", "100, 100", "180, 100"})
  @DisplayName("the constructor clamps every chance to a percentage")
  void clamps(final int given, final int kept) {
    final MobSettings settings =
        new MobSettings(given, given, 30, 15, 8, 14, 60, given, 64, 40, 16, 24);
    assertEquals(kept, settings.vampireBatSpawnChance());
    assertEquals(kept, settings.batEscortChance());
    assertEquals(kept, settings.vampireOutingChance());
  }

  @ParameterizedTest(name = "{0} seconds is kept as {1}")
  @CsvSource({"-5, 0", "0, 0", "30, 30", "3600, 3600", "9000, 3600"})
  @DisplayName("the constructor clamps the levitation timings, and 0 is allowed")
  void clampsLevitation(final int given, final int kept) {
    final MobSettings settings =
        new MobSettings(100, 40, given, given, 8, 14, 60, 50, 64, 40, 16, 24);
    assertEquals(kept, settings.vampireLevitateSeconds());
    assertEquals(kept, settings.vampireLandSeconds());
  }

  @ParameterizedTest(name = "{0} seconds is kept as {1}")
  @CsvSource({"-5, 0", "0, 0", "8, 8", "600, 600", "900, 600"})
  @DisplayName("the constructor clamps the wait between hisses, and 0 is allowed")
  void clampsHiss(final int given, final int kept) {
    assertEquals(
        kept,
        new MobSettings(100, 40, 30, 15, given, 14, 60, 50, 64, 40, 16, 24).vampireHissSeconds());
  }

  @ParameterizedTest(name = "threshold {0} is kept as {1}")
  @CsvSource({"-3, 0", "0, 0", "14, 14", "20, 20", "35, 20"})
  @DisplayName("the hunger threshold stays within a blood bar")
  void clampsHungerThreshold(final int given, final int kept) {
    assertEquals(
        kept,
        new MobSettings(100, 40, 30, 15, 8, given, 60, 50, 64, 40, 16, 24)
            .vampireHungerThreshold());
  }

  @ParameterizedTest(name = "{0} seconds per blood is kept as {1}")
  @CsvSource({"-1, 5", "4, 5", "60, 60", "600, 600", "900, 600"})
  @DisplayName("the blood drain keeps a sane pace")
  void clampsBloodDrain(final int given, final int kept) {
    assertEquals(
        kept,
        new MobSettings(100, 40, 30, 15, 8, 14, given, 50, 64, 40, 16, 24)
            .vampireBloodDrainSeconds());
  }

  @ParameterizedTest(name = "{0} blocks is kept as {1}")
  @CsvSource({"-4, 16", "8, 16", "16, 16", "64, 64", "256, 256", "999, 256"})
  @DisplayName("the feeding and chase ranges reach past a structure but not across the world")
  void clampsRanges(final int given, final int kept) {
    final MobSettings settings =
        new MobSettings(100, 40, 30, 15, 8, 14, 60, 50, given, given, 16, 24);
    assertEquals(kept, settings.vampireFeedingRange());
    assertEquals(kept, settings.vampireChaseRange());
  }

  @ParameterizedTest(name = "{0} blocks is kept as {1}")
  @CsvSource({"2, 8", "8, 8", "16, 16", "64, 64", "90, 64"})
  @DisplayName("a vampire's home covers a room or a whole castle, never the world")
  void clampsHomeRadius(final int given, final int kept) {
    assertEquals(
        kept,
        new MobSettings(100, 40, 30, 15, 8, 14, 60, 50, 64, 40, given, 24).vampireHomeRadius());
  }

  @ParameterizedTest(name = "{0} blocks is kept as {1}")
  @CsvSource({"0, 4", "4, 4", "24, 24", "64, 64", "100, 64"})
  @DisplayName("a hurt vampire calls on housemates within a sane range")
  void clampsAlertRange(final int given, final int kept) {
    assertEquals(
        kept,
        new MobSettings(100, 40, 30, 15, 8, 14, 60, 50, 64, 40, 16, given).vampireAlertRange());
  }
}
