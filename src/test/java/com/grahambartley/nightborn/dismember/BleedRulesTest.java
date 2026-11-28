package com.grahambartley.nightborn.dismember;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.DismembermentSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BleedRulesTest {

  @ParameterizedTest(name = "human {0}s, vampire {1}s, vampire victim {2}: {3} ticks")
  @CsvSource({"60, 20, false, 1200", "60, 20, true, 400", "0, 20, false, 0", "90, 0, true, 0"})
  @DisplayName("a wound bleeds for the human or the vampire bleed time")
  void bleedTicks(final int human, final int vampire, final boolean isVampire, final int ticks) {
    final DismembermentSettings settings =
        new DismembermentSettings(true, true, 15, 15, 50, 35, human, vampire, 3);
    assertEquals(ticks, BleedRules.bleedTicks(settings, isVampire));
  }

  @ParameterizedTest(name = "cut at 100, now {0}, every {1}s: pulse {2}")
  @CsvSource({
    "100, 3, false",
    "159, 3, false",
    "160, 3, true",
    "220, 3, true",
    "221, 3, false",
    "120, 1, true",
    "90, 3, false"
  })
  @DisplayName("a bleed takes its toll once every pulse after the cut, never on the cut itself")
  void pulses(final long now, final int seconds, final boolean pulse) {
    assertEquals(pulse, BleedRules.pulses(100L, now, seconds));
  }

  @ParameterizedTest(name = "first cut at 100, second at {0}, now {1}: pulse {2}")
  @CsvSource({"130, 160, false", "130, 190, true", "200, 260, true", "200, 250, false"})
  @DisplayName("a second cut restarts the pulse for every open wound from that cut")
  void secondCutRestartsThePulse(final long secondCut, final long now, final boolean pulse) {
    assertEquals(pulse, BleedRules.pulses(secondCut, now, 3));
  }

  @ParameterizedTest(name = "health {0} loses {1}")
  @CsvSource({"20, 1", "3, 1", "2.5, 0.5", "2, 0", "1, 0", "0.5, 0"})
  @DisplayName("a human loses half a heart a pulse and never drops below one heart from bleeding")
  void healthLoss(final float health, final float loss) {
    assertEquals(loss, BleedRules.healthLoss(health), 1.0e-6F);
  }

  @ParameterizedTest(name = "health {0} loses {1} to a sear")
  @CsvSource({"20, 2", "3, 2", "2, 1", "1.5, 0.5", "1, 0"})
  @DisplayName("a sear costs a heart but never kills, so a body bled down to one heart survives it")
  void searLoss(final float health, final float loss) {
    assertEquals(loss, BleedRules.searLoss(health), 1.0e-6F);
  }

  @ParameterizedTest(name = "blood {0} leaves {1}")
  @CsvSource({"20, 19", "1, 0", "0, 0"})
  @DisplayName("a vampire loses one blood a pulse and never goes below empty")
  void bloodLeft(final int blood, final int left) {
    assertEquals(left, BleedRules.bloodLeft(blood));
  }
}
