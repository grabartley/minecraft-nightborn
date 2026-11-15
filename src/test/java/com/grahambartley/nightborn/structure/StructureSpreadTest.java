package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StructureSpreadTest {

  @ParameterizedTest(name = "{0} chunks at {1}% is {2}")
  @CsvSource({
    "40, 100, 40",
    "40, 25, 10",
    "40, 400, 160",
    "3, 25, 1",
    "1, 25, 1",
    "4096, 400, 16384"
  })
  @DisplayName("spacing scales with rarity and never drops below one chunk")
  void scalesSpacing(final int base, final int rarity, final int expected) {
    assertEquals(expected, StructureSpread.spacing(base, rarity));
  }

  @ParameterizedTest(name = "separation {1} at {2}% inside spacing {0} is {3}")
  @CsvSource({
    "40, 16, 100, 16",
    "10, 16, 25, 4",
    "160, 16, 400, 64",
    "1, 16, 25, 0",
    "5, 30, 100, 4"
  })
  @DisplayName("separation scales with rarity and always stays below the spacing")
  void scalesSeparation(final int spacing, final int base, final int rarity, final int expected) {
    assertEquals(expected, StructureSpread.separation(spacing, base, rarity));
  }
}
