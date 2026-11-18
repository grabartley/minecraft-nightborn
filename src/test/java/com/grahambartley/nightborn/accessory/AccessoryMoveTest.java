package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AccessoryMoveTest {

  @ParameterizedTest(name = "accessory {0}, fits the armour slot {1}: moves {2}")
  @CsvSource({
    "true, false, true",
    "true, true, false",
    "false, false, false",
    "false, true, false"
  })
  @DisplayName("only an accessory left in an armour slot it no longer fits is moved")
  void misplaced(final boolean accessory, final boolean fits, final boolean moves) {
    assertEquals(moves, AccessoryMove.misplaced(accessory, fits));
  }
}
