package com.grahambartley.nightborn.eldest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EldestRewardsTest {

  @ParameterizedTest(name = "staked={0} sun={1} -> {2}")
  @CsvSource({"true, false, true", "false, true, true", "true, true, true", "false, false, false"})
  @DisplayName("only a stake or the sun pays out for the Eldest")
  void pays(final boolean staked, final boolean sunKill, final boolean pays) {
    assertEquals(pays, EldestRewards.pays(staked, sunKill));
  }
}
