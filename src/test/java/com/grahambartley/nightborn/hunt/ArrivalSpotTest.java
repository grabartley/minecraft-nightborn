package com.grahambartley.nightborn.hunt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ArrivalSpotTest {

  @ParameterizedTest(name = "turn {0}, reach {1}")
  @CsvSource({"0, 0", "0.25, 0.5", "0.5, 1", "0.75, 0.3", "0.9, 0.9", "0.1, -1", "0.6, 2"})
  @DisplayName("he arrives a short way off, never on top of the vampire nor out of sight")
  void shortWayOff(final double turn, final double reach) {
    final BlockPos quarry = new BlockPos(100, 70, -40);
    final BlockPos spot = ArrivalSpot.offset(quarry, turn, reach);
    final double distance = Math.hypot(spot.getX() - quarry.getX(), spot.getZ() - quarry.getZ());
    assertEquals(quarry.getY(), spot.getY());
    assertTrue(distance >= ArrivalSpot.NEAREST - 1.0, "too near: " + distance);
    assertTrue(distance <= ArrivalSpot.FARTHEST + 1.0, "too far: " + distance);
  }

  @ParameterizedTest(name = "turn {0} lies {1}, {2}")
  @CsvSource({"0, 16, 0", "0.25, 0, 16", "0.5, -16, 0", "0.75, 0, -16"})
  @DisplayName("the turn picks the side he comes from")
  void turns(final double turn, final int dx, final int dz) {
    assertEquals(new BlockPos(dx, 0, dz), ArrivalSpot.offset(BlockPos.ORIGIN, turn, 0.0));
  }
}
