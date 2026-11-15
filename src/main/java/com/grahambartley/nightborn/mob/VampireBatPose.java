package com.grahambartley.nightborn.mob;

import software.bernie.geckolib.animation.RawAnimation;

public enum VampireBatPose {
  HANG("hang", true),
  FLY("fly", true),
  BITE("bite", false);

  private final String animation;
  private final RawAnimation clip;

  VampireBatPose(final String animation, final boolean loops) {
    this.animation = animation;
    this.clip =
        loops ? RawAnimation.begin().thenLoop(animation) : RawAnimation.begin().thenPlay(animation);
  }

  public static VampireBatPose of(final boolean roosting, final boolean biting) {
    if (roosting) {
      return HANG;
    }
    return biting ? BITE : FLY;
  }

  public String animation() {
    return animation;
  }

  public RawAnimation clip() {
    return clip;
  }
}
