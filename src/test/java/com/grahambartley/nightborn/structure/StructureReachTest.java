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

  @ParameterizedTest(name = "reach {0} with {1} fits: {2}")
  @CsvSource({
    "128, NONE, true",
    "129, NONE, false",
    "116, BEARD_THIN, true",
    "117, BEARD_THIN, false",
    "116, BURY, true",
    "117, ENCAPSULATE, false",
    "1, BEARD_BOX, true"
  })
  @DisplayName("a structure fits only when its reach and padding stay inside the referenced chunks")
  void fits(final int reach, final StructureTerrainAdaptation adaptation, final boolean fits) {
    assertEquals(fits, StructureReach.fits(reach, adaptation));
  }
}
