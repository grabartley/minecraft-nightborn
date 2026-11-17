package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.age.AgeNeeds;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class AgeSettingsTest {

  static Stream<Setting<?>> settings() {
    return AgeSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(AgeSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @Test
  @DisplayName("the defaults are the locked nights, blood and gifts")
  void lockedDefaults() {
    final AgeSettings age = NightbornConfig.DEFAULTS.age();
    assertEquals(
        List.of(new AgeNeeds(10, 100), new AgeNeeds(40, 500), new AgeNeeds(100, 1500)),
        age.needs());
    assertEquals(4, age.stageHealth());
    assertEquals(1, age.stageDamage());
    assertEquals(2, age.stageBlood());
    assertEquals(10, age.stageDrainSlower());
  }

  @Test
  @DisplayName("values outside their range are clamped on load")
  void clamps() {
    final AgeSettings low = new AgeSettings(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1);
    final AgeSettings high =
        new AgeSettings(99999, 99999, 99999, 999999, 999999, 999999, 99, 99, 99, 99);
    assertEquals(new AgeSettings(0, 0, 0, 0, 0, 0, 0, 0, 0, 0), low);
    assertEquals(
        new AgeSettings(10000, 10000, 10000, 100000, 100000, 100000, 40, 20, 10, 30), high);
  }
}
