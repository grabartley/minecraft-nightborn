package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.mob.Gait;
import software.bernie.geckolib.animation.RawAnimation;

public enum HunterPose {
  IDLE("idle", true),
  WALK("walk", true),
  RUN("run", true),
  STAKE_STRIKE("stake_strike", false),
  TRADE("trade", true);

  private final String animation;
  private final RawAnimation clip;

  HunterPose(final String animation, final boolean loops) {
    this.animation = animation;
    this.clip =
        loops ? RawAnimation.begin().thenLoop(animation) : RawAnimation.begin().thenPlay(animation);
  }

  public String animation() {
    return animation;
  }

  public RawAnimation clip() {
    return clip;
  }

  public static HunterPose of(
      final boolean swinging,
      final boolean trading,
      final boolean sprinting,
      final float limbSwing) {
    if (swinging) {
      return STAKE_STRIKE;
    }
    if (trading) {
      return TRADE;
    }
    if (Gait.of(limbSwing) == Gait.STILL) {
      return IDLE;
    }
    return sprinting ? RUN : WALK;
  }
}
