package com.grahambartley.nightborn.thrall;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ThrallOrdersTest {

  @ParameterizedTest(name = "owner {0}, main hand {1}, empty {2}, sneaking {3} -> {4}")
  @CsvSource({
    "true, true, true, false, true",
    "false, true, true, false, false",
    "true, false, true, false, false",
    "true, true, false, false, false",
    "true, true, true, true, false"
  })
  @DisplayName(
      "only the owner gives orders, with an empty hand and standing, so a sneak still feeds")
  void takesOrder(
      final boolean owner,
      final boolean mainHand,
      final boolean empty,
      final boolean sneaking,
      final boolean expected) {
    assertEquals(expected, ThrallOrders.takesOrder(owner, mainHand, empty, sneaking));
  }
}
