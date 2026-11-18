package com.grahambartley.nightborn.feeding;

public final class BiteGifts {

  private BiteGifts() {}

  public static float feeding(final float worth, final double multiplier) {
    return worth <= 0.0F ? worth : (float) (worth * Math.max(1.0, multiplier));
  }

  public static float finisher(final float worth, final double extra) {
    return worth <= 0.0F ? worth : worth + (float) Math.max(0.0, extra);
  }
}
