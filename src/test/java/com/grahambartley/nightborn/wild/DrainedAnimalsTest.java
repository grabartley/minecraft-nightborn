package com.grahambartley.nightborn.wild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.core.SurfaceSpots;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DrainedAnimalsTest {

  @ParameterizedTest(name = "facing yaw {0} -> falls at {1}, {2}")
  @CsvSource({"0, 0, 20", "90, -20, 0", "180, 0, -20", "-90, 20, 0"})
  @DisplayName("a drained animal falls in front of the player, where they will see it")
  void ahead(final float yaw, final int x, final int z) {
    assertEquals(
        new BlockPos(x, 64, z),
        SurfaceSpots.offset(new BlockPos(0, 64, 0), DrainedAnimals.ahead(yaw, 0.5F), 20));
  }

  @ParameterizedTest(name = "roll {0} turns {1} radians from straight ahead")
  @CsvSource({"0.0, 0.7854", "1.0, 0.7854", "0.5, 0.0"})
  @DisplayName("the spot strays no more than an eighth of a turn either way")
  void spread(final float roll, final float most) {
    assertEquals(
        most, Math.abs(DrainedAnimals.ahead(0.0F, roll) - DrainedAnimals.ahead(0.0F, 0.5F)), 0.001);
  }

  @ParameterizedTest(name = "laid at {0}, now {1}: falls {2}")
  @CsvSource({"100, 99, false", "100, 100, true", "100, 140, true"})
  @DisplayName("a drained animal falls once it has stood for a moment where players can see it")
  void due(final long at, final long now, final boolean falls) {
    assertEquals(falls, DrainedAnimals.due(at, now));
  }
}
