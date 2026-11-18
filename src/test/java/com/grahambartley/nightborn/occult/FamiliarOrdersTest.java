package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FamiliarOrdersTest {

  @ParameterizedTest(
      name = "owner {0}, main {1}, empty {2}, sneaking {3}, carrying {4}, {5} -> {6}")
  @CsvSource({
    "true, true, true, false, false, FIGHT, WHEEL",
    "true, true, true, true, true, FIGHT, TAKE_BACK",
    "true, true, true, true, false, FIGHT, WHEEL",
    "true, true, false, false, false, CARRY, GIVE",
    "true, true, false, false, false, FIGHT, WHEEL",
    "true, true, false, true, false, CARRY, WHEEL",
    "true, false, true, false, false, FIGHT, WHEEL",
    "false, true, true, true, true, FIGHT, WHEEL",
    "false, true, false, false, false, CARRY, WHEEL"
  })
  @DisplayName(
      "only the owner hands items to a carrying familiar or takes them back by sneaking, and"
          + " everything else goes to the command wheel")
  void order(
      final boolean owner,
      final boolean mainHand,
      final boolean empty,
      final boolean sneaking,
      final boolean carrying,
      final FamiliarMode mode,
      final FamiliarOrders.Order expected) {
    assertEquals(expected, FamiliarOrders.order(owner, mainHand, empty, sneaking, carrying, mode));
  }
}
