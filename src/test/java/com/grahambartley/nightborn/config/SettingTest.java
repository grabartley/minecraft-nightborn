package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SettingTest {

  static Stream<Setting<?>> everySetting() {
    return NightbornConfig.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("everySetting")
  @DisplayName("ids, names and tooltips come from section and key")
  void naming(final Setting<?> setting) {
    assertEquals(setting.section() + "." + setting.key(), setting.id());
    assertEquals("nightborn.setting." + setting.id(), setting.translationKey());
    assertEquals(setting.translationKey() + ".tooltip", setting.tooltipKey());
    assertEquals("nightborn.section." + setting.section(), setting.sectionKey());
    assertTrue(setting.key().matches("[a-z][A-Za-z]+"), setting.key());
  }
}
