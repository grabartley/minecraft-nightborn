package com.grahambartley.nightborn.clothing;

public final class ClothingMotion {

  public static final String IDLE = "idle";
  public static final String WALK = "walk";
  static final float WALKING_LIMB_SPEED = 0.1F;

  private ClothingMotion() {}

  public static String clip(final float limbSpeed) {
    return limbSpeed > WALKING_LIMB_SPEED ? WALK : IDLE;
  }
}
