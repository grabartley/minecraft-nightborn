package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ClothingCutTest {

  @ParameterizedTest(name = "slim body {0}: {1} cut")
  @CsvSource({"true, FEMALE, female", "false, MALE, male"})
  @DisplayName("clothing is drawn in the cut that fits the wearer's body")
  void fitsTheBody(final boolean slimBody, final ClothingCut cut, final String id) {
    assertEquals(cut, ClothingCut.fitting(slimBody));
    assertEquals(id, ClothingCut.fitting(slimBody).id());
  }
}
