package com.grahambartley.nightborn.decor;

import java.util.List;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class RackHooks {

  public static final int COUNT = 2;
  private static final List<Vec3d> NORTH_CENTRES =
      List.of(new Vec3d(5.5 / 16, 8.0 / 16, 13.0 / 16), new Vec3d(10.5 / 16, 8.0 / 16, 13.0 / 16));

  private RackHooks() {}

  public static int hookAt(final Direction facing, final Vec3d inBlock) {
    final Vec3d north = FacingShapes.turn(inBlock, back(facing));
    return north.x < 0.5 ? 0 : 1;
  }

  public static Vec3d centre(final Direction facing, final int hook) {
    return FacingShapes.turn(NORTH_CENTRES.get(hook), facing);
  }

  private static Direction back(final Direction facing) {
    return switch (facing) {
      case EAST -> Direction.WEST;
      case WEST -> Direction.EAST;
      default -> facing;
    };
  }
}
