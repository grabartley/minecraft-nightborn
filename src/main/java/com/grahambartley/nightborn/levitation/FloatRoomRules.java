package com.grahambartley.nightborn.levitation;

import java.util.OptionalDouble;
import net.minecraft.util.math.Box;

public final class FloatRoomRules {

  public static final int NODES_AHEAD = 3;
  static final double FIT_MARGIN = 1.0E-3;

  private FloatRoomRules() {}

  public static double groundFeet(
      final double feetY, final OptionalDouble surface, final boolean levitating) {
    if (!levitating) {
      return feetY;
    }
    final double hovered = feetY - LevitationLift.HOVER_HEIGHT;
    return surface.isPresent() ? Math.max(surface.getAsDouble(), hovered) : hovered;
  }

  public static Box floatingBody(
      final double width,
      final double height,
      final double x,
      final double groundFeet,
      final double z) {
    final double half = width / 2.0;
    final double bottom = groundFeet + LevitationLift.HOVER_HEIGHT;
    return new Box(x - half, bottom, z - half, x + half, bottom + height, z + half)
        .contract(FIT_MARGIN);
  }

  public static int lastNodeChecked(final int current, final int length) {
    return Math.min(length, Math.max(0, current) + NODES_AHEAD);
  }
}
