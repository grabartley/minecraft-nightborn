package com.grahambartley.nightborn.blood;

public final class BloodValue {

  public static final int FRESH_PERCENT = 100;

  private BloodValue() {}

  public static float worth(final float taken, final int qualityPercent, final int keptPercent) {
    if (taken <= 0.0F) {
      return 0.0F;
    }
    return taken * qualityPercent / 100.0F * keptPercent / 100.0F;
  }

  public static float taken(final float healthBefore, final float healthAfter) {
    return Math.max(0.0F, healthBefore - Math.max(0.0F, healthAfter));
  }

  public record Split(int whole, float remainder) {}

  public static Split split(final float pending) {
    final int whole = (int) Math.floor(Math.max(0.0F, pending));
    return new Split(whole, Math.max(0.0F, pending) - whole);
  }
}
