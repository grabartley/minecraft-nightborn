package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RackUseTest {

  @ParameterizedTest(name = "weapon={0} emptyHand={1} hookFree={2} -> {3}")
  @CsvSource({
    "true, false, true, HANG",
    "true, false, false, NONE",
    "false, false, true, NONE",
    "false, true, false, TAKE",
    "false, true, true, NONE",
    "false, false, false, NONE"
  })
  void decidesWhatAUseDoesToAHook(
      final boolean weapon, final boolean emptyHand, final boolean hookFree, final String want) {
    final Optional<RackUse> expected =
        "NONE".equals(want) ? Optional.empty() : Optional.of(RackUse.valueOf(want));
    assertEquals(expected, RackUse.decide(weapon, emptyHand, hookFree));
  }
}
