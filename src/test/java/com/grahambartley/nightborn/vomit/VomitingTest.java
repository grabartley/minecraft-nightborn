package com.grahambartley.nightborn.vomit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VomitingTest {

  @ParameterizedTest(name = "creative {0}, used up {1}, no container {2}: {3}")
  @CsvSource({
    "true, false, false, KEEP",
    "true, true, true, KEEP",
    "false, true, false, HAND_BACK_CONTAINER",
    "false, true, true, HAND_BACK_CONTAINER",
    "false, false, false, POCKET_CONTAINER",
    "false, false, true, KEEP"
  })
  @DisplayName(
      "what is left in hand matches eating it: creative keeps all, a last item leaves its container")
  void leftover(
      final boolean creative,
      final boolean usedUp,
      final boolean noContainer,
      final Vomiting.Leftover leftover) {
    assertEquals(leftover, Vomiting.leftover(creative, usedUp, noContainer));
  }
}
