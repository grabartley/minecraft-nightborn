package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StakingTest {

  @ParameterizedTest(name = "already staked {0}, stake strike {1}, stake ammo {2}: {3}")
  @CsvSource({
    "false, true, false, true",
    "false, false, true, true",
    "false, false, false, false",
    "true, true, false, false",
    "true, false, true, false"
  })
  @DisplayName(
      "a stake strike or stake bolt stakes a downed vampire, but the staking itself never does")
  void isStakeBlow(
      final boolean alreadyStaked, final boolean strike, final boolean ammo, final boolean stakes) {
    assertEquals(stakes, Staking.isStakeBlow(alreadyStaked, strike, ammo));
  }
}
