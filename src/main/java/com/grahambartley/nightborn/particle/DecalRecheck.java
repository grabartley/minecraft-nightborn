package com.grahambartley.nightborn.particle;

public final class DecalRecheck {

  public static final int EVERY_TICKS = 5;

  private DecalRecheck() {}

  public static int phase(final int roll) {
    return Math.floorMod(roll, EVERY_TICKS);
  }

  public static boolean due(final int age, final int phase) {
    return Math.floorMod(age + phase, EVERY_TICKS) == 0;
  }
}
