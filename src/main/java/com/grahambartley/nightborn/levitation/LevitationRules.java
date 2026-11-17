package com.grahambartley.nightborn.levitation;

public final class LevitationRules {

  public static final int TOGGLE_COOLDOWN_TICKS = 10;

  private LevitationRules() {}

  public static boolean mayLevitate(
      final boolean vampire, final int weakened, final boolean downed, final boolean occupied) {
    return vampire && weakened <= 0 && !downed && !occupied;
  }

  public static boolean movesAsGrounded(final boolean onGround, final boolean levitating) {
    return onGround || levitating;
  }

  public static boolean sinksFast(
      final boolean levitating, final boolean player, final boolean sneaking) {
    return levitating && player && sneaking;
  }

  public static boolean landsOnSneak(final boolean sinksFast, final boolean onGround) {
    return sinksFast && onGround;
  }

  public static boolean crouches(final boolean crouching, final boolean levitating) {
    return crouching && !levitating;
  }

  public static boolean toggleReady(final Long lastToggle, final long now) {
    return lastToggle == null || now < lastToggle || now - lastToggle >= TOGGLE_COOLDOWN_TICKS;
  }
}
