package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FogZonesTest {

  private static final FogZone WEST = new FogZone(-50.0, 64.0, 0.0, 40.0);
  private static final FogZone EAST = new FogZone(50.0, 64.0, 0.0, 80.0);
  private static final List<FogZone> ZONES = List.of(WEST, EAST);

  @ParameterizedTest(name = "at x {0} the zone is {1}")
  @CsvSource({
    "-50, WEST",
    "-30, WEST",
    "-20, WEST",
    "-10, EAST",
    "0, EAST",
    "50, EAST",
    "-95, NONE",
    "200, NONE"
  })
  @DisplayName("the zone a player is deepest inside wins, and none is chosen outside them all")
  void choosesTheDeepest(final double x, final String expected) {
    final Optional<FogZone> found = FogZones.nearest(ZONES, x, 64.0, 0.0);
    assertEquals(
        switch (expected) {
          case "WEST" -> Optional.of(WEST);
          case "EAST" -> Optional.of(EAST);
          default -> Optional.empty();
        },
        found);
  }

  @ParameterizedTest(name = "at x {0}")
  @CsvSource({"0", "100", "-100"})
  @DisplayName("no zones give no fog")
  void emptyIsEmpty(final double x) {
    assertEquals(Optional.empty(), FogZones.nearest(List.of(), x, 64.0, 0.0));
  }
}
