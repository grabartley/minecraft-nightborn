package com.grahambartley.nightborn.slayer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DashStateTest {

  private static final DashPower SIDESTEP = new DashPower(4.0F, 6, 5, 30, 8.0F);

  @ParameterizedTest(name = "started {0}, now {1}: untouchable {2}, dodges {3}, over {4}")
  @CsvSource({
    "100, 100, true, true, false",
    "100, 105, true, true, false",
    "100, 106, false, false, true",
    "100, 103, true, true, false",
    "100, 200, false, false, true"
  })
  @DisplayName(
      "nothing touches a dashing hunter for its untouchable time, and a blow in its first moment"
          + " is a dodge")
  void windows(
      final long started,
      final long now,
      final boolean untouchable,
      final boolean dodges,
      final boolean over) {
    final DashState state = new DashState(started, SIDESTEP);
    assertEquals(untouchable, state.untouchable(now));
    assertEquals(dodges, state.dodges(now));
    assertEquals(over, state.over(now));
  }
}
