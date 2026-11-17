package com.grahambartley.nightborn.guild;

public final class Aim {

  static final double ARC = 0.2;

  private Aim() {}

  public static double rise(
      final double aimY, final double fromY, final double dx, final double dz) {
    return aimY - fromY + Math.sqrt(dx * dx + dz * dz) * ARC;
  }
}
