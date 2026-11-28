package com.grahambartley.nightborn.client.particle;

public final class DecalLift {

  static final double ANCHOR = 0.002;
  static final double RIM_DRY = 0.002;
  static final double RIM_WET = 0.006;
  static final double BODY_DRY = 0.010;
  static final double BODY_WET = 0.020;

  private DecalLift() {}

  public static double anchor() {
    return ANCHOR;
  }

  public static double rim(final float wetness) {
    return between(RIM_DRY, RIM_WET, wetness);
  }

  public static double body(final float wetness) {
    return between(BODY_DRY, BODY_WET, wetness);
  }

  private static double between(final double dry, final double wet, final float wetness) {
    return dry + (wet - dry) * Math.min(1.0F, Math.max(0.0F, wetness));
  }
}
