package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.downed.DownedState.Phase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DownedMobRulesTest {

  @ParameterizedTest(name = "state {0}: helpless {1}, flees {2}")
  @CsvSource({"STANDING, false, false", "DOWNED, true, false", "ESCAPING, false, true"})
  @DisplayName("a downed vampire mob lies helpless and an escaping one flees")
  void behaviour(final String state, final boolean helpless, final boolean flees) {
    final DownedState current =
        state.equals("STANDING") ? null : new DownedState(Phase.valueOf(state), 20);
    assertEquals(helpless, DownedMobRules.helpless(current));
    assertEquals(flees, DownedMobRules.flees(current));
  }
}
