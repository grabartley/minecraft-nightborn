package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RelocationTest {

  @ParameterizedTest(name = "slot free {0}, inventory room {1}: {2}")
  @CsvSource({
    "true, true, SLOT",
    "true, false, SLOT",
    "false, true, INVENTORY",
    "false, false, FEET"
  })
  @DisplayName("a moved piece goes to its new slot, else the inventory, else the player's feet")
  void where(final boolean slotFree, final boolean room, final Relocation expected) {
    assertEquals(expected, Relocation.of(slotFree, room));
  }
}
