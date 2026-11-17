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
    final StructureSettings settings = new StructureSettings(given, given, 300, 24);
    assertEquals(kept, settings.structureRarity());
    assertEquals(kept, settings.campRarity());
  }

  @ParameterizedTest(name = "an interval of {0} and a range of {1} are kept as {2} and {3}")
  @CsvSource({"0, 0, 20, 8", "20, 8, 20, 8", "300, 24, 300, 24", "9000, 200, 3600, 64"})
  @DisplayName("the constructor clamps how often and how near a structure gramophone plays")
  void clampsGramophones(
      final int seconds, final int range, final int keptSeconds, final int keptRange) {
    final StructureSettings settings = new StructureSettings(100, 100, seconds, range);
    assertEquals(keptSeconds, settings.gramophoneSeconds());
    assertEquals(keptRange, settings.gramophoneRange());
  }
}
