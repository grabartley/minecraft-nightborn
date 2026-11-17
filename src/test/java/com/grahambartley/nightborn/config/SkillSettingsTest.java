package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SkillSettingsTest {

  static Stream<Setting<?>> settings() {
    return SkillSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(SkillSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @Test
  @DisplayName("the constructor clamps values into bounds")
  void clamps() {
    assertEquals(
        new SkillSettings(1, 0, 0, 0, 0, 0, 0, 0),
        new SkillSettings(0, -1, -1, -1, -1, -1, -1, -1));
    assertEquals(
        new SkillSettings(100000, 100, 1000, 10000, 10000, 10000, 100, 10000),
        new SkillSettings(100001, 101, 1001, 10001, 10001, 10001, 101, 10001));
  }
}
