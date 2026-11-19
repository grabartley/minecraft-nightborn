package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class CoffinSettingsTest {

  static Stream<Setting<?>> settings() {
    return CoffinSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(CoffinSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  static Stream<Arguments> shares() {
    return Stream.of(
        Arguments.of(-5, 0),
        Arguments.of(0, 0),
        Arguments.of(50, 50),
        Arguments.of(100, 100),
        Arguments.of(150, 100));
  }

  @ParameterizedTest(name = "{0} becomes {1}")
  @MethodSource("shares")
  @DisplayName("the sleep share is clamped to a percentage")
  void clampsTheShare(final int given, final int kept) {
    assertEquals(kept, new CoffinSettings(given, 50, 10, 1, 15, 2).sleepShare());
  }

  @ParameterizedTest(name = "window {0} s becomes {1} s")
  @CsvSource({"-1, 0", "0, 0", "50, 50", "600, 600", "601, 600"})
  @DisplayName("the whole day window is clamped")
  void clampsTheWindow(final int given, final int kept) {
    assertEquals(kept, new CoffinSettings(50, given, 10, 1, 15, 2).wholeDayWindow());
  }

  @ParameterizedTest(name = "rested {0}% {1} and at home {2}% {3}")
  @CsvSource({
    "-1, -1, -1, -1, 0, 0, 0, 0",
    "10, 1, 15, 2, 10, 1, 15, 2",
    "500, 99, 500, 99, 100, 20, 100, 20"
  })
  @DisplayName("what being rested gives is clamped")
  void clampsTheRest(
      final int speed,
      final int damage,
      final int homeSpeed,
      final int homeDamage,
      final int keptSpeed,
      final int keptDamage,
      final int keptHomeSpeed,
      final int keptHomeDamage) {
    final CoffinSettings settings =
        new CoffinSettings(50, 50, speed, damage, homeSpeed, homeDamage);
    assertEquals(keptSpeed, settings.restedSpeed());
    assertEquals(keptDamage, settings.restedDamage());
    assertEquals(keptHomeSpeed, settings.houseRestedSpeed());
    assertEquals(keptHomeDamage, settings.houseRestedDamage());
  }

  @ParameterizedTest(name = "{0} defaults to {1}")
  @CsvSource({
    "sleepShare, 50",
    "wholeDayWindow, 50",
    "restedSpeed, 10",
    "restedDamage, 1",
    "houseRestedSpeed, 15",
    "houseRestedDamage, 2"
  })
  @DisplayName("the locked defaults hold")
  void lockedDefaults(final String key, final int value) {
    assertEquals(
        value,
        CoffinSettings.SETTINGS.stream()
            .filter(setting -> setting.key().equals(key))
            .findFirst()
            .orElseThrow()
            .defaultValue());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("the whole-config list carries the coffin settings")
  void isListedInTheConfig(final Setting<?> setting) {
    assertEquals(true, NightbornConfig.SETTINGS.contains(setting));
  }
}
