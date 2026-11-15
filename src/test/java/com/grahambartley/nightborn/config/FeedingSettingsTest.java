package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class FeedingSettingsTest {

  static Stream<Setting<?>> settings() {
    return FeedingSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(FeedingSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @Test
  @DisplayName("the constructor clamps values into bounds")
  void clamps() {
    final FeedingSettings low = new FeedingSettings(0, -1, -1, -1, 0);
    assertEquals(new FeedingSettings(1, 0, 0, 0, 1), low);
    final FeedingSettings high = new FeedingSettings(50, 500, 5000, 500, 50);
    assertEquals(new FeedingSettings(20, 100, 600, 64, 20), high);
  }
}
