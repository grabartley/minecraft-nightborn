package com.grahambartley.nightborn.hunt;

import net.minecraft.util.math.MathHelper;

public record BellCall(int elapsed, int total) {

  static final int ANSWER_INTERVAL = 40;
  static final double FARTHEST_ANSWER = 48.0;
  static final double NEAREST_ANSWER = 8.0;
  static final int MOST_LIGHT = 10;
  static final int CHECK_TICKS = 20;

  public static BellCall rung(final int total) {
    return new BellCall(0, total);
  }

  public static boolean calls(
      final boolean hasSeal,
      final boolean inWindow,
      final boolean overworld,
      final boolean openSky,
      final boolean busy) {
    return hasSeal && !busy && HuntRules.canArrive(overworld, openSky, inWindow);
  }

  public static boolean holds(
      final boolean alive, final boolean openSky, final boolean inWindow, final boolean done) {
    return alive && openSky && (inWindow || !done);
  }

  public boolean checks() {
    return elapsed % CHECK_TICKS == 0;
  }

  public boolean triesArrival() {
    return done() && (elapsed - total) % CHECK_TICKS == 0;
  }

  public BellCall advanced() {
    return new BellCall(elapsed + 1, total);
  }

  public float progress() {
    return total <= 0 ? 1.0F : MathHelper.clamp((float) elapsed / total, 0.0F, 1.0F);
  }

  public boolean done() {
    return elapsed >= total;
  }

  public boolean answers() {
    return !done() && elapsed % ANSWER_INTERVAL == 0;
  }

  public double answerDistance() {
    return MathHelper.lerp(progress(), FARTHEST_ANSWER, NEAREST_ANSWER);
  }

  public int light() {
    return 1 + Math.round(progress() * (MOST_LIGHT - 1));
  }
}
