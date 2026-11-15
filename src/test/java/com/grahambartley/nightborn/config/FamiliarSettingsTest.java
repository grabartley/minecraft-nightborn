package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class FamiliarSettingsTest {

  static Stream<Setting<?>> settings() {
    return FamiliarSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(FamiliarSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @ParameterizedTest(name = "{0} is kept as {1}")
  @CsvSource({"-20, 0", "0, 0", "55, 55", "100, 100", "180, 100"})
  @DisplayName("the constructor clamps loyalty values and chances to 0..100")
  void clampsLoyalty(final int given, final int kept) {
    final FamiliarSettings settings =
        new FamiliarSettings(given, 1, 7, given, given, given, given, given, given, 8, 32);
    assertEquals(kept, settings.startLoyalty());
    assertEquals(kept, settings.paidDayLoyalty());
    assertEquals(kept, settings.unpaidDayLoyalty());
    assertEquals(kept, settings.bittenLoyalty());
    assertEquals(kept, settings.hurtLoyalty());
    assertEquals(kept, settings.betrayLoyalty());
    assertEquals(kept, settings.betrayChance());
  }

  @ParameterizedTest(name = "{0} is kept as wage {1}, ahead {2}, guard {3}, errand {4}")
  @CsvSource({"-5, 1, 1, 1, 4", "0, 1, 1, 1, 4", "10, 10, 10, 10, 10", "500, 30, 120, 32, 128"})
  @DisplayName("the constructor clamps wages and ranges to their bounds")
  void clampsRanges(
      final int given, final int wage, final int ahead, final int guard, final int errand) {
    final FamiliarSettings settings =
        new FamiliarSettings(60, given, given, 5, 15, 25, 10, 25, 30, given, given);
    assertEquals(wage, settings.wageDaysPerEmerald());
    assertEquals(ahead, settings.wageDaysAhead());
    assertEquals(guard, settings.guardRange());
    assertEquals(errand, settings.errandRange());
  }
}
