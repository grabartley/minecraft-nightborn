package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ClothingFabricTest {

  @ParameterizedTest(name = "{0}: dyeable {1}")
  @CsvSource({"CLOTH, true", "LEATHER, false"})
  @DisplayName("cloth takes dye and leather keeps its own colour")
  void dyeable(final ClothingFabric fabric, final boolean dyeable) {
    assertEquals(dyeable, fabric.dyeable());
  }
}
