package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class FirstNightSettingsTest {

  static Stream<Setting<?>> settings() {
    return FirstNightSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(FirstNightSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @ParameterizedTest(name = "{0} nights at {1}% are kept as {2} nights at {3}%")
  @CsvSource({"-1, -1, 0, 0", "5, 50, 5, 50", "90, 300, 30, 100"})
  @DisplayName("the constructor clamps how long a player is new and how often the watcher comes")
  void clampsWatcherNights(
      final int nights, final int chance, final int keptNights, final int keptChance) {
    final FirstNightSettings settings = new FirstNightSettings(nights, chance, 24, 12, 5, 128);
    assertEquals(keptNights, settings.firstNights());
    assertEquals(keptChance, settings.watcherChance());
  }

  @ParameterizedTest(name = "a distance of {0} and a leave distance of {1} are kept as {2} and {3}")
  @CsvSource({"24, 12, 24, 12", "0, 0, 8, 2", "8, 30, 8, 7", "100, 100, 64, 32"})
  @DisplayName("the watcher always leaves before a player reaches the distance it keeps")
  void clampsWatcherDistances(
      final int distance, final int leave, final int keptDistance, final int keptLeave) {
    final FirstNightSettings settings = new FirstNightSettings(5, 50, distance, leave, 5, 128);
    assertEquals(keptDistance, settings.watcherDistance());
    assertEquals(keptLeave, settings.watcherLeaveDistance());
  }

  @ParameterizedTest(name = "a chance of {0} and a range of {1} are kept as {2} and {3}")
  @CsvSource({"-5, 0, 0, 32", "5, 128, 5, 128", "500, 9000, 100, 512"})
  @DisplayName("the constructor clamps how often and how far round a structure signs are found")
  void clampsSigns(final int chance, final int range, final int keptChance, final int keptRange) {
    final FirstNightSettings settings = new FirstNightSettings(5, 50, 24, 12, chance, range);
    assertEquals(keptChance, settings.signChance());
    assertEquals(keptRange, settings.signRange());
  }
}
