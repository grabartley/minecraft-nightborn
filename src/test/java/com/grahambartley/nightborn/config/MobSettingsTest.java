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
    final MobSettings settings = new MobSettings(given, given, given, 30, 15, 8);
    assertEquals(kept, settings.vampireSpawnChance());
    assertEquals(kept, settings.vampireBatSpawnChance());
    assertEquals(kept, settings.batEscortChance());
  }

  @ParameterizedTest(name = "{0} seconds is kept as {1}")
  @CsvSource({"-5, 0", "0, 0", "30, 30", "3600, 3600", "9000, 3600"})
  @DisplayName("the constructor clamps the levitation timings, and 0 is allowed")
  void clampsLevitation(final int given, final int kept) {
    final MobSettings settings = new MobSettings(100, 100, 40, given, given, 8);
    assertEquals(kept, settings.vampireLevitateSeconds());
    assertEquals(kept, settings.vampireLandSeconds());
  }

  @ParameterizedTest(name = "{0} seconds is kept as {1}")
  @CsvSource({"-5, 0", "0, 0", "8, 8", "600, 600", "900, 600"})
  @DisplayName("the constructor clamps the wait between hisses, and 0 is allowed")
  void clampsHiss(final int given, final int kept) {
    assertEquals(kept, new MobSettings(100, 100, 40, 30, 15, given).vampireHissSeconds());
  }
}
