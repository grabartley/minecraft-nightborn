package com.grahambartley.nightborn.decor;

import java.util.List;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class RackHooks {

  public static final int COUNT = 2;
  private static final double BETWEEN_ROWS = 7.5 / 16;
  private static final List<Vec3d> NORTH_CENTRES =
      List.of(new Vec3d(8.0 / 16, 11.0 / 16, 12.5 / 16), new Vec3d(8.0 / 16, 5.0 / 16, 12.5 / 16));

  private RackHooks() {}

  public static int hookAt(final double heightInBlock) {
    return heightInBlock >= BETWEEN_ROWS ? 0 : 1;
  }

  public static Vec3d centre(final Direction facing, final int hook) {
    return FacingShapes.turn(NORTH_CENTRES.get(hook), facing);
  }
}
