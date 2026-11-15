package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StakingTest {

  @ParameterizedTest(name = "melee {0}, stake in hand {1}: stake blow {2}")
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("only a melee hit with a stake in hand is a stake blow")
  void isStakeBlow(final boolean melee, final boolean stakeInHand, final boolean blow) {
    assertEquals(blow, Staking.isStakeBlow(melee, stakeInHand));
  }
}
