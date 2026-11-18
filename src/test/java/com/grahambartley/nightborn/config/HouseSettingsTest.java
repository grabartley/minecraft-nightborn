package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class HouseSettingsTest {

  static Stream<Setting<?>> settings() {
    return HouseSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(HouseSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @ParameterizedTest(name = "age {0} -> {1} members")
  @CsvSource({"-1, 0", "0, 0", "1, 2", "2, 4", "3, 6", "4, 6", "9, 6"})
  @DisplayName("a house holds as many members as its lord's age allows, by default")
  void membersByAge(final int age, final int members) {
    assertEquals(members, NightbornConfig.DEFAULTS.houses().members(age));
  }

  @ParameterizedTest(name = "{0} kept as {1}")
  @CsvSource({"-3, 0", "0, 0", "5, 5", "32, 32", "40, 32"})
  @DisplayName("each stage's member count stays in range")
  void clamps(final int members, final int kept) {
    assertEquals(kept, new HouseSettings(members, members, members, members).adultMembers());
  }
}
