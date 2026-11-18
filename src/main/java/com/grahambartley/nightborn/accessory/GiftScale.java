package com.grahambartley.nightborn.accessory;

public final class GiftScale {

  private GiftScale() {}

  public static int ticks(final int ticks, final double factor) {
    if (ticks <= 0) {
      return ticks;
    }
    return (int) Math.max(1L, Math.round(ticks * Math.max(0.0, factor)));
  }

  public static float amount(final float amount, final double factor) {
    return amount * (float) Math.max(0.0, factor);
  }

  public static int count(final int count, final double factor) {
    return count <= 0 ? count : (int) Math.round(count * Math.max(0.0, factor));
  }
}
