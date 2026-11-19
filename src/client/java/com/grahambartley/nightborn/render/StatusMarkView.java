package com.grahambartley.nightborn.render;

import net.minecraft.util.math.MathHelper;

public final class StatusMarkView {

  static final float NET_SPREAD = 1.15F;
  static final float NET_RIG_HEIGHT = 17.0F / 16.0F;
  static final float HEX_LIFT = 0.3F;
  static final float HEX_SPREAD = 1.8F;
  static final float HEX_MIN = 0.6F;
  static final float HEX_MAX = 1.6F;
  static final float QUARRY_SPREAD = 1.6F;
  static final float QUARRY_MIN = 1.0F;
  static final float QUARRY_MAX = 3.0F;

  private StatusMarkView() {}

  public static boolean shows(final boolean hidden, final Long until, final long now) {
    return !hidden && until != null && now < until;
  }

  public static float netWidth(final float width) {
    return width * NET_SPREAD;
  }

  public static float netHeight(final float height) {
    return height / NET_RIG_HEIGHT;
  }

  public static float hexLift(final float height) {
    return height + HEX_LIFT;
  }

  public static float hexScale(final float width) {
    return MathHelper.clamp(width * HEX_SPREAD, HEX_MIN, HEX_MAX);
  }

  public static float quarryScale(final float width) {
    return MathHelper.clamp(width * QUARRY_SPREAD, QUARRY_MIN, QUARRY_MAX);
  }
}
