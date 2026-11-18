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
    final StructureSettings settings =
        new StructureSettings(given, given, 300, 24, 1024, 15, 40, 2, 128);
    assertEquals(kept, settings.structureRarity());
    assertEquals(kept, settings.campRarity());
  }

  @ParameterizedTest(name = "an interval of {0} and a range of {1} are kept as {2} and {3}")
  @CsvSource({"0, 0, 20, 8", "20, 8, 20, 8", "300, 24, 300, 24", "9000, 200, 3600, 64"})
  @DisplayName("the constructor clamps how often and how near a structure gramophone plays")
  void clampsGramophones(
      final int seconds, final int range, final int keptSeconds, final int keptRange) {
    final StructureSettings settings =
        new StructureSettings(100, 100, seconds, range, 1024, 15, 40, 2, 128);
    assertEquals(keptSeconds, settings.gramophoneSeconds());
    assertEquals(keptRange, settings.gramophoneRange());
  }

  @ParameterizedTest(name = "a rumour range of {0} is kept as {1}")
  @CsvSource({"0, 128", "128, 128", "1024, 1024", "4096, 4096", "9000, 4096"})
  @DisplayName("the constructor clamps how far a Guild Master's rumours reach")
  void clampsRumours(final int range, final int kept) {
    assertEquals(
        kept, new StructureSettings(100, 100, 300, 24, range, 15, 40, 2, 128).rumourRange());
  }

  @ParameterizedTest(name = "{0} to {1} days are kept as {2} to {3}")
  @CsvSource({"0, 0, 1, 1", "15, 40, 15, 40", "30, 10, 30, 30", "500, 900, 365, 365"})
  @DisplayName(
      "the constructor clamps how long a cleared structure stays empty, and never inverts it")
  void clampsRefill(final int min, final int max, final int keptMin, final int keptMax) {
    final StructureSettings settings =
        new StructureSettings(100, 100, 300, 24, 1024, min, max, 2, 128);
    assertEquals(keptMin, settings.refillMinDays());
    assertEquals(keptMax, settings.refillMaxDays());
  }

  @ParameterizedTest(name = "a chance of {0} and a distance of {1} are kept as {2} and {3}")
  @CsvSource({"-1, 0, 0, 64", "2, 128, 2, 128", "150, 900, 100, 512"})
  @DisplayName("the constructor clamps how often and how far away wild vampires appear")
  void clampsWildVampires(
      final int chance, final int distance, final int keptChance, final int keptDistance) {
    final StructureSettings settings =
        new StructureSettings(100, 100, 300, 24, 1024, 15, 40, chance, distance);
    assertEquals(keptChance, settings.wildVampireChance());
    assertEquals(keptDistance, settings.wildVampireDistance());
  }
}
