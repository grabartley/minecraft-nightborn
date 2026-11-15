package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RackHooksTest {

  @ParameterizedTest(name = "facing {0}, hook {1}")
  @CsvSource({
    "NORTH, 0",
    "NORTH, 1",
    "EAST, 0",
    "EAST, 1",
    "SOUTH, 0",
    "SOUTH, 1",
    "WEST, 0",
    "WEST, 1"
  })
  void clickingWhereAWeaponRestsPicksItsHook(final Direction facing, final int hook) {
    assertEquals(hook, RackHooks.hookAt(RackHooks.centre(facing, hook).y));
  }

  @ParameterizedTest(name = "height {0} is hook {1}")
  @CsvSource({"0.95, 0", "0.6, 0", "0.5, 0", "0.49, 1", "0.3, 1", "0.02, 1"})
  void theUpperHalfIsTheUpperRow(final double height, final int hook) {
    assertEquals(hook, RackHooks.hookAt(height));
  }

  @ParameterizedTest(name = "facing {0}")
  @CsvSource({"NORTH", "EAST", "SOUTH", "WEST"})
  void restsWeaponsJustInFrontOfThePegs(final Direction facing) {
    for (int hook = 0; hook < RackHooks.COUNT; hook++) {
      final Vec3d centre = RackHooks.centre(facing, hook);
      final Vec3d wall =
          new Vec3d(0.5, 0.5, 0.5).add(Vec3d.of(facing.getOpposite().getVector()).multiply(0.5));
      final double fromWall =
          Math.abs(centre.subtract(wall).dotProduct(Vec3d.of(facing.getVector())));
      assertEquals(3.5 / 16, fromWall, 1.0e-9);
      assertTrue(centre.x > 0 && centre.x < 1 && centre.z > 0 && centre.z < 1, centre.toString());
    }
  }
}
