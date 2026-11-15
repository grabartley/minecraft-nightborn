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
    final MobSettings settings = new MobSettings(given, given, given);
    assertEquals(kept, settings.vampireSpawnChance());
    assertEquals(kept, settings.vampireBatSpawnChance());
    assertEquals(kept, settings.batEscortChance());
  }
}
