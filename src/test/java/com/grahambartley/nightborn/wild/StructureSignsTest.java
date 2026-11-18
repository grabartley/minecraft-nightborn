package com.grahambartley.nightborn.wild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StructureSignsTest {

  @ParameterizedTest(name = "{0} structures in range, disconnected {1}: {2}")
  @CsvSource({"1, false, true", "3, false, true", "0, false, false", "1, true, false"})
  @DisplayName("signs show only for a player still here with a vampire structure in range")
  void nearOne(final int structures, final boolean disconnected, final boolean shows) {
    assertEquals(shows, StructureSigns.nearOne(structures, disconnected));
  }
}
