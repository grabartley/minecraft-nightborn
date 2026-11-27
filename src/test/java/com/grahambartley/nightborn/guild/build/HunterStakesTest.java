package com.grahambartley.nightborn.guild.build;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HunterStakesTest {

  @ParameterizedTest(name = "vampire {0}, staked {1}, grows {2} -> pays {3}")
  @CsvSource({
    "true, true, true, true",
    "false, true, true, false",
    "true, false, true, false",
    "true, true, false, false",
    "false, false, false, false"
  })
  @DisplayName("a hunter mob earns stake experience only for staking a vampire")
  void paysOnlyForAStakedVampire(
      final boolean vampire, final boolean staked, final boolean grows, final boolean pays) {
    assertEquals(pays, HunterStakes.pays(vampire, staked, grows));
  }
}
