package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EditAccessTest {

  @ParameterizedTest(name = "in world {0}, operator {1}: {2}")
  @CsvSource({
    "true, true, EDITABLE",
    "true, false, READ_ONLY",
    "false, false, NO_WORLD",
    "false, true, NO_WORLD"
  })
  @DisplayName("only an operator in a world may edit, and the screen says why otherwise")
  void resolves(final boolean inWorld, final boolean canEdit, final EditAccess expected) {
    assertEquals(expected, EditAccess.resolve(inWorld, canEdit));
    assertEquals(expected == EditAccess.EDITABLE, expected.bannerKey().isEmpty());
  }
}
