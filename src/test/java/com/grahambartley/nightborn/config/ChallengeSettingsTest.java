package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class ChallengeSettingsTest {

  static Stream<Setting<?>> settings() {
    return ChallengeSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(ChallengeSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @ParameterizedTest(name = "{0} on offer every {1} days is kept as {2} every {3}")
  @CsvSource({"3, 7, 3, 7", "-1, 0, 0, 1", "99, 999, 10, 100"})
  void clampsToItsBounds(final int offer, final int days, final int keptOffer, final int keptDays) {
    final ChallengeSettings settings = new ChallengeSettings(offer, days);
    assertEquals(keptOffer, settings.trialsOnOffer());
    assertEquals(keptDays, settings.trialDays());
  }
}
