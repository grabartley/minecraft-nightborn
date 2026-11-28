package com.grahambartley.nightborn.client.particle;

public final class DecalLift {

  public static final double ANCHOR = 0.004;
  static final double RIM_DRY = 0.008;
  static final double RIM_WET = 0.012;
  static final double BODY_DRY = 0.018;
  static final double BODY_WET = 0.028;

  private DecalLift() {}

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
