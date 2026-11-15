package com.grahambartley.nightborn.client;

public final class SicknessVignette {

  static final float FIRST_DAY = 0.25F;
  static final float PER_DAY = 0.2F;
  static final float MOST = 0.85F;

  private SicknessVignette() {}

  public static float alpha(final int dayIndex) {
    return Math.min(MOST, FIRST_DAY + Math.max(0, dayIndex) * PER_DAY);
  }
}
