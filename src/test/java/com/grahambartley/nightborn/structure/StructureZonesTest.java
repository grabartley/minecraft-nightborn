package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StructureZonesTest {

  private static final StructureZone WEST =
      new StructureZone(
          Identifier.of("nightborn", "castle"),
          FogZone.around(
              new BlockBox(-55, 60, -5, -45, 70, 5), new BlockBox(-60, 60, -10, -40, 70, 10)),
          StructureAmbience.SILENT);
  private static final StructureZone EAST =
      new StructureZone(
          Identifier.of("nightborn", "underground_lair"),
          FogZone.around(
              new BlockBox(45, 60, -5, 55, 70, 5), new BlockBox(10, 60, -10, 90, 70, 10)),
          StructureAmbience.SILENT);
  private static final List<StructureZone> ZONES = List.of(WEST, EAST);

  @ParameterizedTest(name = "at x {0} the zone is {1}")
  @CsvSource({"-50, WEST", "-35, WEST", "-5, EAST", "50, EAST", "-90, NONE", "120, NONE"})
  @DisplayName("the zone a player is deepest inside wins, and none is chosen outside them all")
  void choosesTheDeepest(final double x, final String expected) {
    assertEquals(
        switch (expected) {
          case "WEST" -> Optional.of(WEST);
          case "EAST" -> Optional.of(EAST);
          default -> Optional.empty();
        },
        StructureZones.deepest(ZONES, x, 65.0, 0.0));
  }

  @ParameterizedTest(name = "at x {0}")
  @CsvSource({"0", "100", "-100"})
  @DisplayName("no zones give no zone")
  void emptyIsEmpty(final double x) {
    assertEquals(Optional.empty(), StructureZones.deepest(List.of(), x, 65.0, 0.0));
  }
}
