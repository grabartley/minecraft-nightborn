package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.gen.StructureTerrainAdaptation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class StructureReachTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(StructureTerrainAdaptation.class)
  @DisplayName("only a structure that reshapes the land needs padding")
  void pads(final StructureTerrainAdaptation adaptation) {
    assertEquals(
        adaptation == StructureTerrainAdaptation.NONE ? 0 : 12, StructureReach.padding(adaptation));
  }

  @ParameterizedTest(name = "reach {0} with {1} and a summit search of {2} fits: {3}")
  @CsvSource({
    "128, NONE, 0, true",
    "129, NONE, 0, false",
    "116, BEARD_THIN, 0, true",
    "117, BEARD_THIN, 0, false",
    "116, BURY, 0, true",
    "117, ENCAPSULATE, 0, false",
    "1, BEARD_BOX, 0, true",
    "64, NONE, 48, true",
    "64, NONE, 64, true",
    "65, NONE, 64, false",
    "64, BEARD_THIN, 53, false"
  })
  @DisplayName(
      "a structure fits only when its reach, padding and summit search stay inside the referenced"
          + " chunks")
  void fits(
      final int reach,
      final StructureTerrainAdaptation adaptation,
      final int summitReach,
      final boolean fits) {
    assertEquals(fits, StructureReach.fits(reach, adaptation, summitReach));
  }
}
