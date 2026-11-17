package com.grahambartley.nightborn.ritual;

import net.minecraft.util.math.MathHelper;

public record RitualBuildUp(int elapsed, int total) {

  static final int TOLL_INTERVAL = 40;
  static final float HIGHEST_TOLL = 0.9F;
  static final float LOWEST_TOLL = 0.5F;
  static final int MOST_SMOKE = 12;
  static final double MOST_GUTTERING = 0.2;

  public float progress() {
    return total <= 0 ? 1.0F : MathHelper.clamp((float) elapsed / total, 0.0F, 1.0F);
  }

  public boolean done() {
    return elapsed >= total;
  }

  public boolean tolls() {
    return !done() && elapsed % TOLL_INTERVAL == 0;
  }

  public float tollPitch() {
    return MathHelper.lerp(progress(), HIGHEST_TOLL, LOWEST_TOLL);
  }

  public int smoke() {
    return 1 + Math.round(progress() * (MOST_SMOKE - 1));
  }

  public boolean gutters(final double roll) {
    return roll < progress() * MOST_GUTTERING;
  }
}
