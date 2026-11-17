package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PowerBarRowTest {

  @ParameterizedTest(name = "mouse at {0},{1}: slot {2}")
  @CsvSource({
    "100, 50, 0",
    "121, 71, 0",
    "122, 60, -1",
    "124, 60, 1",
    "196, 60, 4",
    "217, 60, 4",
    "218, 60, -1",
    "99, 60, -1",
    "110, 49, -1",
    "110, 72, -1"
  })
  @DisplayName(
      "the mouse is over a slot only inside its square, not in the gaps or outside the row")
  void slotAt(final int mouseX, final int mouseY, final int slot) {
    assertEquals(slot, new PowerBarRow(100, 50).slotAt(mouseX, mouseY));
  }

  @ParameterizedTest(name = "centre {0}")
  @CsvSource({"159", "200", "0"})
  @DisplayName("the five slots sit centred in a row")
  void centred(final int centre) {
    final PowerBarRow row = PowerBarRow.centred(centre, 10);
    assertEquals(centre - PowerBarRow.WIDTH / 2, row.slotX(0));
    assertEquals(row.left() + PowerBarRow.WIDTH, row.slotX(4) + PowerBarRow.SLOT);
  }
}
