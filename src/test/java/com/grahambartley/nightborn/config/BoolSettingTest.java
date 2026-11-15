package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class BoolSettingTest {

  @ParameterizedTest(name = "default {0}")
  @ValueSource(booleans = {true, false})
  void missingIsDefault(final boolean defaultValue) {
    assertEquals(
        defaultValue, new BoolSetting("test", "flag", defaultValue, c -> true).normalise(null));
  }

  @ParameterizedTest(name = "{0} stays {0}")
  @CsvSource({"true", "false"})
  void keepsGivenValue(final boolean value) {
    assertEquals(value, new BoolSetting("test", "flag", !value, c -> true).normalise(value));
  }

  @ParameterizedTest(name = "reads {0}")
  @ValueSource(booleans = {true, false})
  void readsThroughItsReader(final boolean showInList) {
    final NightbornConfig config =
        NightbornConfig.DEFAULTS.with(FactionSettings.SHOW_IN_PLAYER_LIST, showInList);
    assertEquals(showInList, FactionSettings.SHOW_IN_PLAYER_LIST.get(config));
  }
}
