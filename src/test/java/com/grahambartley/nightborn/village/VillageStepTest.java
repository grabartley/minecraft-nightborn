package com.grahambartley.nightborn.village;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.config.VillageSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VillageStepTest {

  private static final VillageSettings DEFAULTS = NightbornConfig.DEFAULTS.villages();

  @ParameterizedTest(name = "suspicion {0} is {1}")
  @CsvSource({
    "0, CALM",
    "24, CALM",
    "25, WARY",
    "49, WARY",
    "50, WARDED",
    "74, WARDED",
    "75, HUNTED",
    "100, HUNTED"
  })
  @DisplayName("the step is set by suspicion alone, at 25, 50 and 75 by default")
  void stepOfSuspicion(final int suspicion, final VillageStep step) {
    assertEquals(step, VillageStep.of(suspicion, DEFAULTS));
  }

  @ParameterizedTest(name = "suspicion {0} with steps at 10, 20, 30 is {1}")
  @CsvSource({"9, CALM", "10, WARY", "20, WARDED", "30, HUNTED"})
  @DisplayName("the steps follow the server's settings")
  void followsSettings(final int suspicion, final VillageStep step) {
    assertEquals(
        step,
        VillageStep.of(
            suspicion, new VillageSettings(10, 20, 30, 2, 5, 2, 3, 10, 25, 48, 5, 10, 3)));
  }

  @ParameterizedTest(name = "{0} at least {1}: {2}")
  @CsvSource({
    "CALM, WARY, false",
    "WARY, WARY, true",
    "WARDED, WARY, true",
    "HUNTED, WARDED, true",
    "WARDED, HUNTED, false"
  })
  void comparesSteps(final VillageStep step, final VillageStep other, final boolean atLeast) {
    assertEquals(atLeast, step.atLeast(other));
  }

  @ParameterizedTest(name = "{0} to {1} has eased: {2}")
  @CsvSource({
    "WARY, CALM, true",
    "HUNTED, CALM, true",
    "WARY, WARY, false",
    "WARDED, WARY, false",
    "CALM, CALM, false",
    "CALM, WARY, false"
  })
  @DisplayName("a village has eased only when it drops from Wary or worse to calm")
  void eased(final VillageStep before, final VillageStep after, final boolean eased) {
    assertEquals(eased, VillageStep.eased(before, after));
  }
}
