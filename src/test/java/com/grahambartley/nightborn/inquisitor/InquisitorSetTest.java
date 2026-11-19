package com.grahambartley.nightborn.inquisitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.InquisitorSettings;
import com.grahambartley.nightborn.config.NightbornConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InquisitorSetTest {

  @ParameterizedTest(name = "vampire {0}, greatsword {1}, full set {2}: {3}%")
  @CsvSource({
    "true, true, false, 10",
    "true, true, true, 20",
    "true, false, false, 0",
    "true, false, true, 0",
    "false, true, false, 0",
    "false, true, true, 0"
  })
  @DisplayName(
      "the greatsword gives a vampire a tenth of each blow as blood, a fifth with the set,"
          + " and a human nothing")
  void share(
      final boolean vampire, final boolean blade, final boolean complete, final int percent) {
    assertEquals(
        percent,
        InquisitorSet.share(vampire, blade, complete, NightbornConfig.DEFAULTS.inquisitor()));
  }

  @ParameterizedTest(name = "vampire {0}, full set {1}: {2} extra blood, drain change {3}")
  @CsvSource({
    "true, true, 4, -0.25",
    "true, false, 0, 0",
    "false, true, 0, 0",
    "false, false, 0, 0"
  })
  @DisplayName("only a vampire in the whole set holds more blood and loses it more slowly")
  void setBonus(
      final boolean vampire, final boolean complete, final int extra, final double drain) {
    final InquisitorSettings settings = NightbornConfig.DEFAULTS.inquisitor();
    assertEquals(extra, InquisitorSet.extraBlood(vampire, complete, settings));
    assertEquals(drain, InquisitorSet.drainChange(vampire, complete, settings), 1.0E-9);
  }

  @ParameterizedTest(name = "greatsword {0}%, set {1}%, extra {2}, slower {3}%")
  @CsvSource({"0, 0, 0, 0", "50, 100, 20, 80", "5, 15, 2, 10"})
  @DisplayName("every amount follows the server's settings")
  void followsSettings(final int greatsword, final int set, final int extra, final int slower) {
    final InquisitorSettings settings =
        NightbornConfig.DEFAULTS
            .with(InquisitorSettings.GREATSWORD_BLOOD, greatsword)
            .with(InquisitorSettings.SET_BLOOD, set)
            .with(InquisitorSettings.SET_EXTRA_BLOOD, extra)
            .with(InquisitorSettings.SET_DRAIN_SLOWER, slower)
            .inquisitor();
    assertEquals(greatsword, InquisitorSet.share(true, true, false, settings));
    assertEquals(set, InquisitorSet.share(true, true, true, settings));
    assertEquals(extra, InquisitorSet.extraBlood(true, true, settings));
    assertEquals(-slower / 100.0, InquisitorSet.drainChange(true, true, settings), 1.0E-9);
  }
}
