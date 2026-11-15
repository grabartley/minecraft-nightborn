package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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
    assertEquals(kept, new CoffinSettings(given).sleepShare());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("the whole-config list carries the coffin settings")
  void isListedInTheConfig(final Setting<?> setting) {
    assertEquals(true, NightbornConfig.SETTINGS.contains(setting));
  }
}
