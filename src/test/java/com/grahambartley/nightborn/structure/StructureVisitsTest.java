package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Optional;
import net.minecraft.util.math.BlockBox;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StructureVisitsTest {

  private static final Optional<FogZone> ZONE =
      Optional.of(
          FogZone.around(new BlockBox(8, 0, 8, 12, 4, 12), new BlockBox(0, 0, 0, 19, 9, 19)));

  @ParameterizedTest(name = "({0}, {1}, {2}) inside: {3}")
  @CsvSource({
    "10, 2, 10, true",
    "0, 0, 0, true",
    "20, 10, 20, true",
    "0.5, 9.5, 19.5, true",
    "-0.5, 2, 10, false",
    "20.5, 2, 10, false",
    "10, 2, -12, false",
    "10, 12, 10, false",
    "-23, 2, 10, false"
  })
  void countsOnlyThePlayerInsideTheStructureBox(
      final double x, final double y, final double z, final boolean inside) {
    assertEquals(inside, StructureVisits.inside(ZONE, x, y, z));
  }

  @Test
  void noZoneMeansNoVisit() {
    assertFalse(StructureVisits.inside(Optional.empty(), 10, 2, 10));
  }
}
