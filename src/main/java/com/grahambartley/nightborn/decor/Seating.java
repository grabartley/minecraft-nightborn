package com.grahambartley.nightborn.decor;

import java.util.List;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public final class Seating {

  public enum Choice {
    SIT,
    PASS,
    TAKEN
  }

  private Seating() {}

  public static Choice choose(final boolean sneaking, final boolean riding, final boolean taken) {
    if (sneaking || riding) {
      return Choice.PASS;
    }
    return taken ? Choice.TAKEN : Choice.SIT;
  }

  static final float HEAD_TURN = 60.0f;

  public static float headYaw(final float seatYaw, final float headYaw) {
    return seatYaw
        + MathHelper.clamp(MathHelper.wrapDegrees(headYaw - seatYaw), -HEAD_TURN, HEAD_TURN);
  }

  public static List<Direction> dismountSides(final Direction facing) {
    return List.of(
        facing, facing.rotateYClockwise(), facing.rotateYCounterclockwise(), facing.getOpposite());
  }

  public static boolean keepsSeat(final boolean hasSitter, final boolean onChair) {
    return hasSitter && onChair;
  }
}
