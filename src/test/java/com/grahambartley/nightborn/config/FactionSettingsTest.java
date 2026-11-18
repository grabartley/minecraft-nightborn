package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class FactionSettingsTest {

  static Stream<Setting<?>> settings() {
    return FactionSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(FactionSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("the toggle is kept as given")
  void keepsToggle(final boolean shown) {
    assertEquals(shown, new FactionSettings(shown, 5, 3, 32).showInPlayerList());
  }

  static Stream<Arguments> clamped() {
    return Stream.of(
        Arguments.of(new FactionSettings(false, 0, 0, -1), new FactionSettings(false, 1, 1, 0)),
        Arguments.of(
            new FactionSettings(false, 21, 11, 129), new FactionSettings(false, 20, 10, 128)),
        Arguments.of(new FactionSettings(true, 5, 3, 32), new FactionSettings(true, 5, 3, 32)));
  }

  @ParameterizedTest(name = "{0} becomes {1}")
  @MethodSource("clamped")
  @DisplayName("the ceremony numbers are clamped into bounds")
  void clampsCeremony(final FactionSettings given, final FactionSettings expected) {
    assertEquals(expected, given);
  }
}
