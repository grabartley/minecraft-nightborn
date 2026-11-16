package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FamiliarOrdersTest {

  @ParameterizedTest(name = "owner {0}, main {1}, empty {2}, sneaking {3}, {4} -> {5}")
  @CsvSource({
    "true, true, true, false, FIGHT, NEXT_MODE",
    "true, true, true, true, FIGHT, TAKE_BACK",
    "true, true, false, false, CARRY, GIVE",
    "true, true, false, false, FIGHT, NONE",
    "true, true, false, true, CARRY, NONE",
    "true, false, true, false, FIGHT, NONE",
    "false, true, true, false, FIGHT, NONE",
    "false, true, false, false, CARRY, NONE"
  })
  @DisplayName(
      "only the owner gives orders: an empty hand changes its task or takes back what it carries,"
          + " and an item is handed over only while it carries")
  void order(
      final boolean owner,
      final boolean mainHand,
      final boolean empty,
      final boolean sneaking,
      final FamiliarMode mode,
      final FamiliarOrders.Order expected) {
    assertEquals(expected, FamiliarOrders.order(owner, mainHand, empty, sneaking, mode));
  }
}
