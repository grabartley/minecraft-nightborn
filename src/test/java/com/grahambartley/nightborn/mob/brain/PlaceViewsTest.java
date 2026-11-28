package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PlaceViewsTest {

  @ParameterizedTest(name = "open [{0}]: room {1}")
  @CsvSource({"'', 0", "1, 1", "111, 3", "1111, 4", "11111, 4", "101, 1", "0111, 0"})
  @DisplayName("room to fly counts the open blocks over the head, up to the first ceiling")
  void room(final String open, final int room) {
    assertEquals(
        room, PlaceViews.room(step -> step < open.length() && open.charAt(step) == '1', 4));
  }

  @ParameterizedTest(name = "home {0}: {1}")
  @CsvSource({"true, 5", "false, NaN"})
  @DisplayName("the distance home is missing for a mob with no home")
  void home(final boolean hasHome, final double distance) {
    assertEquals(
        distance,
        PlaceViews.home(hasHome, new Vec3d(0.5, 64, 0.5), new BlockPos(3, 64, 4)),
        1.0E-9);
  }
}
