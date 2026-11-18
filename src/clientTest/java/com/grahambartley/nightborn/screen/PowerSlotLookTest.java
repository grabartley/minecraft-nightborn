package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PowerSlotLookTest {

  @ParameterizedTest(name = "filled {0}, lifted {1}, target {2} -> {3}")
  @CsvSource({
    "false, false, false, EMPTY",
    "true, false, false, FILLED",
    "true, true, false, LIFTED",
    "false, true, false, LIFTED",
    "false, false, true, TARGET",
    "true, false, true, TARGET",
    "true, true, true, TARGET"
  })
  @DisplayName(
      "a slot under a dragged power shows where it lands, then a lifted slot, then its fill")
  void of(
      final boolean filled, final boolean lifted, final boolean target, final PowerSlotLook look) {
    assertEquals(look, PowerSlotLook.of(filled, lifted, target));
  }
}
