package com.grahambartley.nightborn.client.gore;

import com.grahambartley.nightborn.client.state.GoreLevel;

public enum SplatterLook {
  FRESH(true, 0xFFFFFF, 1.0F),
  DRIED(true, 0x6E5248, 0.5F),
  HIDDEN(false, 0xFFFFFF, 0.0F);

  private final boolean drawn;
  private final int tint;
  private final float lifeShare;

  SplatterLook(final boolean drawn, final int tint, final float lifeShare) {
    this.drawn = drawn;
    this.tint = tint;
    this.lifeShare = lifeShare;
  }

  public static SplatterLook of(final GoreLevel gore) {
    return switch (gore) {
      case FULL -> FRESH;
      case REDUCED -> DRIED;
      case OFF -> HIDDEN;
    };
  }

  public static boolean redrawNeeded(final GoreLevel before, final GoreLevel after) {
    return of(before) != of(after);
  }

  public boolean drawn() {
    return this.drawn;
  }

  public float lifeShare() {
    return this.lifeShare;
  }

  public boolean tinted() {
    return this.tint != 0xFFFFFF;
  }

  public int apply(final int argb) {
    final int alpha = argb >>> 24;
    final int red = channel(argb >> 16, this.tint >> 16);
    final int green = channel(argb >> 8, this.tint >> 8);
    final int blue = channel(argb, this.tint);
    return alpha << 24 | red << 16 | green << 8 | blue;
  }

  public float[] rgb(final float shade) {
    return new float[] {
      ((this.tint >> 16) & 0xFF) / 255.0F * shade,
      ((this.tint >> 8) & 0xFF) / 255.0F * shade,
      (this.tint & 0xFF) / 255.0F * shade
    };
  }

  private static int channel(final int value, final int tint) {
    return (value & 0xFF) * (tint & 0xFF) / 0xFF;
  }
}
