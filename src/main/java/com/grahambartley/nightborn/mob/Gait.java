package com.grahambartley.nightborn.mob;

public enum Gait {
  STILL,
  WALK,
  RUN;

  static final float RUN_SWING = 0.6F;
  static final float WALK_SWING = 0.1F;

  public static Gait of(final float limbSwing) {
    if (limbSwing > RUN_SWING) {
      return RUN;
    }
    return limbSwing > WALK_SWING ? WALK : STILL;
  }
}
