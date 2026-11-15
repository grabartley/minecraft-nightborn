package com.grahambartley.nightborn.mob;

import software.bernie.geckolib.animation.RawAnimation;

public enum VampirePose {
  IDLE("idle", true),
  WALK("walk", true),
  RUN("run", true),
  ATTACK("attack", false),
  DOWNED("downed", false);

  static final float RUN_SWING = 0.6F;
  static final float WALK_SWING = 0.1F;

  private final String animation;
  private final RawAnimation clip;

  VampirePose(final String animation, final boolean loops) {
    this.animation = animation;
    this.clip =
        loops
            ? RawAnimation.begin().thenLoop(animation)
            : RawAnimation.begin().thenPlayAndHold(animation);
  }

  public static VampirePose of(
      final boolean downed, final boolean attacking, final float limbSwing) {
    if (downed) {
      return DOWNED;
    }
    if (attacking) {
      return ATTACK;
    }
    if (limbSwing > RUN_SWING) {
      return RUN;
    }
    return limbSwing > WALK_SWING ? WALK : IDLE;
  }

  public String animation() {
    return animation;
  }

  public RawAnimation clip() {
    return clip;
  }
}
