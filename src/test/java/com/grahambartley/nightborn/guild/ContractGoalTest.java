package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ContractGoalTest {

  @ParameterizedTest(name = "{0} counts a stake with cleared {1}: {2}")
  @CsvSource({
    "STAKE_VAMPIRES, false, true",
    "STAKE_VAMPIRES, true, true",
    "CLEAR_HOUSEHOLD, false, false",
    "CLEAR_HOUSEHOLD, true, true"
  })
  void clearingNeedsTheLastOfAHousehold(
      final ContractGoal goal, final boolean cleared, final boolean counted) {
    assertEquals(counted, goal.countedBy(new HunterContract.Stake(0, cleared)));
  }

  @ParameterizedTest(name = "{0} is written {1}")
  @CsvSource({"STAKE_VAMPIRES, stake_vampires", "CLEAR_HOUSEHOLD, clear_household"})
  void writesItsDataName(final ContractGoal goal, final String name) {
    assertEquals(name, goal.asString());
  }
}
