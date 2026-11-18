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
    assertEquals(
        kept, new HouseSettings(members, members, members, members, 3, 7, 6, 8).adultMembers());
  }

  @ParameterizedTest(name = "answer {0}, repeat {1}, reach {2}, radius {3}")
  @CsvSource({
    "0, -1, 1, 3, 1, 0, 2, 4",
    "3, 7, 6, 8, 3, 7, 6, 8",
    "99, 99, 99, 99, 30, 60, 32, 32"
  })
  @DisplayName("the challenge clock and the duel circle stay in range")
  void clampsChallenges(
      final int answer,
      final int repeat,
      final int reach,
      final int radius,
      final int answerKept,
      final int repeatKept,
      final int reachKept,
      final int radiusKept) {
    final HouseSettings settings = new HouseSettings(2, 2, 2, 2, answer, repeat, reach, radius);
    assertEquals(answerKept, settings.answerNights());
    assertEquals(repeatKept, settings.repeatNights());
    assertEquals(reachKept, settings.duelReach());
    assertEquals(radiusKept, settings.duelRadius());
  }
}
