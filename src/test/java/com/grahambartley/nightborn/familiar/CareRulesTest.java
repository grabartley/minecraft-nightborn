package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CareRulesTest {

  @ParameterizedTest(
      name = "owner {0}, main hand {1}, emerald {2}, empty {3}, off-hand emerald {4}: {5}")
  @CsvSource({
    "true, true, true, false, false, PAY",
    "true, false, true, false, false, PAY",
    "true, true, false, true, false, MOOD",
    "true, true, false, true, true, NONE",
    "true, false, false, true, false, NONE",
    "true, true, false, false, false, NONE",
    "false, true, true, false, false, NONE",
    "false, true, false, true, false, NONE"
  })
  @DisplayName("only the owner pays or asks after mood, and an off-hand emerald pays")
  void choose(
      final boolean owner,
      final boolean mainHand,
      final boolean emerald,
      final boolean empty,
      final boolean offHandEmerald,
      final CareRules.Care care) {
    assertEquals(care, CareRules.choose(owner, mainHand, emerald, empty, offHandEmerald));
  }
}
