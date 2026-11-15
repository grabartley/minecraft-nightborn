package com.grahambartley.nightborn.clothing;

import java.util.Locale;

public enum ClothingMotion {
  IDLE,
  WALK;

  static final float WALKING_LIMB_SPEED = 0.1F;

  public static ClothingMotion of(final float limbSpeed) {
    return limbSpeed > WALKING_LIMB_SPEED ? WALK : IDLE;
  }

  public String clip() {
    return name().toLowerCase(Locale.ROOT);
  }
}
