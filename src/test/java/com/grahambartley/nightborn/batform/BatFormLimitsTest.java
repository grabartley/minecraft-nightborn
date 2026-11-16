package com.grahambartley.nightborn.batform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.ActionResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BatFormLimitsTest {

  @ParameterizedTest(name = "bat {0} -> {1}")
  @CsvSource({"true, FAIL", "false, PASS"})
  @DisplayName("a bat is refused every use of an item, block or creature, and others pass through")
  void verdict(final boolean bat, final ActionResult expected) {
    assertEquals(expected, BatFormLimits.verdict(bat));
  }
}
