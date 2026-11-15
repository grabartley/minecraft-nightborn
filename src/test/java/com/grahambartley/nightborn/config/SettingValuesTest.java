package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SettingValuesTest {

  static Stream<Setting<?>> everySetting() {
    return NightbornConfig.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("everySetting")
  @DisplayName("an empty set of values reads every setting's default")
  void emptyReadsDefaults(final Setting<?> setting) {
    assertEquals(setting.defaultValue(), SettingValues.EMPTY.get(setting));
  }

  @Test
  @DisplayName("with clamps and leaves the original untouched")
  void withClampsAndCopies() {
    final SettingValues changed = SettingValues.EMPTY.with(FeedingSettings.DRINK_PER_SECOND, 99);
    assertEquals(20, changed.get(FeedingSettings.DRINK_PER_SECOND));
    assertEquals(2, SettingValues.EMPTY.get(FeedingSettings.DRINK_PER_SECOND));
  }

  @Test
  @DisplayName("of reads back the values a config holds")
  void ofReadsConfig() {
    final NightbornConfig config =
        NightbornConfig.DEFAULTS
            .with(SunSettings.SUN_DAMAGE, 7)
            .with(DownedSettings.HARDCORE_VAMPIRE_DEATH, true);
    final SettingValues values = SettingValues.of(config);
    assertEquals(7, values.get(SunSettings.SUN_DAMAGE));
    assertEquals(true, values.get(DownedSettings.HARDCORE_VAMPIRE_DEATH));
    assertFalse(SettingValues.EMPTY.get(DownedSettings.HARDCORE_VAMPIRE_DEATH));
  }
}
