package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class StructureSettingsTest {

  static Stream<Setting<?>> settings() {
    return StructureSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(StructureSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @ParameterizedTest(name = "{0} is kept as {1}")
  @CsvSource({"-5, 25", "10, 25", "25, 25", "100, 100", "400, 400", "900, 400"})
  @DisplayName(
      "the constructor clamps both rarities to between a quarter and four times the spacing")
  void clamps(final int given, final int kept) {
    final StructureSettings settings = new StructureSettings(given, given);
    assertEquals(kept, settings.structureRarity());
    assertEquals(kept, settings.campRarity());
  }
}
