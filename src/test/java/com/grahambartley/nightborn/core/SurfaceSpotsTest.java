package com.grahambartley.nightborn.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SurfaceSpotsTest {

  @ParameterizedTest(name = "{0} quarter turns, {1} blocks -> {2}, {3}")
  @CsvSource({"0, 10, 110, 50", "1, 10, 100, 60", "2, 24, 76, 50", "3, 128, 100, -78"})
  @DisplayName("a spot lies the distance away from the centre in the angle's direction")
  void offset(final int quarters, final int distance, final int x, final int z) {
    final BlockPos spot =
        SurfaceSpots.offset(new BlockPos(100, 64, 50), quarters * MathHelper.HALF_PI, distance);
    assertEquals(new BlockPos(x, 64, z), spot);
  }
}
