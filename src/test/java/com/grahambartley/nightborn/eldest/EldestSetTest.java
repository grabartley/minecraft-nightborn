package com.grahambartley.nightborn.eldest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.config.RitualSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EldestSetTest {

  @ParameterizedTest(name = "{0} dealt, blade {1}, full set {2}: {3} stolen")
  @CsvSource({
    "10, true, false, 1.0",
    "10, true, true, 2.0",
    "10, false, false, 0.0",
    "10, false, true, 0.0",
    "0, true, true, 0.0",
    "-3, true, true, 0.0",
    "7.5, true, false, 0.75"
  })
  @DisplayName(
      "the blade gives back a tenth of each blow by default, a fifth with the full set, and"
          + " nothing without the blade")
  void stolen(final float dealt, final boolean blade, final boolean complete, final float stolen) {
    final RitualSettings settings = NightbornConfig.DEFAULTS.ritual();
    assertEquals(stolen, EldestSet.stolen(dealt, blade, complete, settings), 1.0E-6F);
  }

  @ParameterizedTest(name = "blade at {0}% and set at {1}%")
  @CsvSource({"0, 0", "50, 100", "100, 100"})
  @DisplayName("the shares stolen follow the server's settings")
  void followsSettings(final int bladeShare, final int setShare) {
    final RitualSettings settings =
        NightbornConfig.DEFAULTS
            .with(RitualSettings.BLADE_LIFE_STEAL, bladeShare)
            .with(RitualSettings.SET_LIFE_STEAL, setShare)
            .ritual();
    assertEquals(8.0F * bladeShare / 100.0F, EldestSet.stolen(8.0F, true, false, settings));
    assertEquals(8.0F * setShare / 100.0F, EldestSet.stolen(8.0F, true, true, settings));
  }
}
