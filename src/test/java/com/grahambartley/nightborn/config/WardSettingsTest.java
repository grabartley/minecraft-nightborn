package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class WardSettingsTest {

  static Stream<Setting<?>> settings() {
    return WardSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(WardSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  static Stream<Arguments> prdRanges() {
    return Stream.of(
        Arguments.of(WardSettings.STRING_RANGE, 8),
        Arguments.of(WardSettings.CRUCIFIX_RANGE, 6),
        Arguments.of(WardSettings.SHAPE_RANGE, 8));
  }

  @ParameterizedTest(name = "{0} = {1}")
  @MethodSource("prdRanges")
  @DisplayName("ranges start at the distances the PRD names")
  void rangesMatchThePrd(final IntSetting setting, final int blocks) {
    assertEquals(blocks, setting.get(NightbornConfig.DEFAULTS));
  }

  @Test
  @DisplayName("touching garlic weakens far more than one string in range")
  void touchOutweighsAString() {
    final WardSettings defaults = NightbornConfig.DEFAULTS.wards();
    assertEquals(true, defaults.touchStrength() > defaults.stringCap());
    assertEquals(true, defaults.stringCap() >= 3 * defaults.stringStrength());
  }

  @Test
  @DisplayName("the constructor clamps values into bounds")
  void clamps() {
    assertEquals(
        new WardSettings(1, 0, 0, 0, 1, 1, 0, 1, 0, 0),
        new WardSettings(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1));
    assertEquals(
        new WardSettings(32, 100, 100, 100, 30, 32, 100, 32, 100, 100),
        new WardSettings(99, 999, 999, 999, 99, 99, 999, 99, 999, 999));
  }
}
