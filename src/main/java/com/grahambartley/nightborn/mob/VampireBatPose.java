package com.grahambartley.nightborn.mob;

import software.bernie.geckolib.animation.RawAnimation;

public enum VampireBatPose {
  HANG("hang", true),
  FLY("fly", true),
  SWOOP("swoop", true),
  BITE("bite", false);

  private final String animation;
  private final RawAnimation clip;

  VampireBatPose(final String animation, final boolean loops) {
    this.animation = animation;
    this.clip =
        loops ? RawAnimation.begin().thenLoop(animation) : RawAnimation.begin().thenPlay(animation);
  }

  public static VampireBatPose of(
      final boolean roosting, final boolean biting, final boolean hunting) {
    if (roosting) {
      return HANG;
    }
    if (biting) {
      return BITE;
    }
    return hunting ? SWOOP : FLY;
  }

  public String animation() {
    return animation;
  }

  public RawAnimation clip() {
    return clip;
  }
}
