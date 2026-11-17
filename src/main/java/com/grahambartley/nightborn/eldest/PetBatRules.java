package com.grahambartley.nightborn.eldest;

import net.minecraft.util.math.Vec3d;

public final class PetBatRules {

  static final double ORBIT = 1.6;
  static final double ABOVE = 0.6;
  static final double TURN = 0.08;
  static final double FAR = 24.0;

  private PetBatRules() {}

  public static Vec3d perch(final Vec3d ownerEyes, final int age) {
    final double angle = age * TURN;
    return ownerEyes.add(Math.cos(angle) * ORBIT, ABOVE, Math.sin(angle) * ORBIT);
  }

  public static boolean lost(final double squaredDistance) {
    return squaredDistance > FAR * FAR;
  }

  public static boolean comesHome(final boolean owner, final boolean sneaking) {
    return owner && sneaking;
  }
}
