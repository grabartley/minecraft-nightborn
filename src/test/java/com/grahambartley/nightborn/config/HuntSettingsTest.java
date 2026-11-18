package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class HuntSettingsTest {

  static Stream<Setting<?>> settings() {
    return HuntSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(HuntSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @ParameterizedTest(name = "raid {0}, hunt {1} kept as {2}, {3}")
  @CsvSource({"15, 30, 15, 30", "40, 30, 40, 40", "0, 0, 1, 1", "2000, 5, 1000, 1000"})
  @DisplayName("the hunt never starts below the raid, and both stay in range")
  void ordersThresholds(final int raid, final int hunt, final int keptRaid, final int keptHunt) {
    final HuntSettings settings = new HuntSettings(raid, hunt, 1, 10, 3, 18000, 200);
    assertEquals(keptRaid, settings.raidNotoriety());
    assertEquals(keptHunt, settings.huntNotoriety());
  }

  @ParameterizedTest(name = "latest arrival {0} kept as {1}")
  @CsvSource({"0, 13001", "13000, 13001", "18000, 18000", "23000, 23000", "24000, 23000"})
  @DisplayName("he may only be set to come between dusk and dawn")
  void clampsArrival(final int latest, final int kept) {
    assertEquals(kept, new HuntSettings(15, 30, 1, 10, 3, latest, 200).latestArrival());
  }

  @ParameterizedTest(name = "warning nights {0} kept as {1}")
  @CsvSource({"-1, 0", "0, 0", "3, 3", "11, 10"})
  @DisplayName("warning nights can be turned off and never run past ten")
  void clampsWarnings(final int nights, final int kept) {
    assertEquals(kept, new HuntSettings(15, 30, 1, 10, nights, 18000, 200).warningNights());
  }
}
