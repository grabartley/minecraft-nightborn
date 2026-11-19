package com.grahambartley.nightborn.mob;

public final class IdleFidget {

  static final int CYCLE_TICKS = 240;
  static final int LOOK_TICKS = 60;
  static final int LISTEN_TICKS = 52;
  static final int ADJUST_TICKS = 48;
  static final int CHOICES = 4;
  static final int SPREAD = 97;

  private IdleFidget() {}

  public static VampirePose pose(final int seed, final long age, final boolean calm) {
    if (!calm) {
      return VampirePose.IDLE;
    }
    final long time = age + Math.floorMod((long) seed * SPREAD, CYCLE_TICKS);
    final long cycle = Math.floorDiv(time, CYCLE_TICKS);
    final long phase = Math.floorMod(time, CYCLE_TICKS);
    final int choice = Math.floorMod(Long.hashCode(cycle * 31 + seed), CHOICES);
    if (choice == 0 && phase < LOOK_TICKS) {
      return VampirePose.IDLE_LOOK;
    }
    if (choice == 1 && phase < LISTEN_TICKS) {
      return VampirePose.IDLE_LISTEN;
    }
    if (choice == 2 && phase < ADJUST_TICKS) {
      return VampirePose.ADJUST_CLOTHES;
    }
    return VampirePose.IDLE;
  }

  public static boolean calm(
      final boolean swinging,
      final boolean attacking,
      final boolean acting,
      final boolean feeding) {
    return !swinging && !attacking && !acting && !feeding;
  }
}
