package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CoffinOwnerDeathsTest {

  @ParameterizedTest(name = "sun {0}, staked {1}: frees {2}")
  @CsvSource({"true, false, true", "false, true, true", "true, true, true", "false, false, false"})
  @DisplayName("only death by the sun or a stake frees a vampire's coffin")
  void freesTheCoffinOnlyForAFinalDeath(
      final boolean sun, final boolean staked, final boolean frees) {
    assertEquals(frees, CoffinOwnerDeaths.frees(sun, staked));
  }
}
