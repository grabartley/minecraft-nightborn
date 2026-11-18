package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CareRulesTest {

  @ParameterizedTest(name = "owner {0}, main hand {1}, emerald {2}, off-hand emerald {3}: {4}")
  @CsvSource({
    "true, true, true, false, PAY",
    "true, false, true, false, PAY",
    "true, true, true, true, PAY",
    "true, true, false, true, OTHER_HAND",
    "true, false, false, true, NONE",
    "true, true, false, false, NONE",
    "false, true, true, false, NONE",
    "false, true, false, true, NONE"
  })
  @DisplayName(
      "only the owner pays, an off-hand emerald pays in its turn, and anything else opens the"
          + " wheel")
  void choose(
      final boolean owner,
      final boolean mainHand,
      final boolean emerald,
      final boolean offHandEmerald,
      final CareRules.Care care) {
    assertEquals(care, CareRules.choose(owner, mainHand, emerald, offHandEmerald));
  }
}
