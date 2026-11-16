package com.grahambartley.nightborn.occult;

import software.bernie.geckolib.animation.RawAnimation;

public enum FamiliarPose {
  APPEAR("appear", false),
  FADE("fade", false),
  FIGHT("fight", false),
  SCOUT("scout", true),
  CARRY("carry", true),
  FLOAT("float", true);

  static final int APPEAR_TICKS = 20;

  private final String animation;
  private final RawAnimation clip;

  FamiliarPose(final String animation, final boolean loops) {
    this.animation = animation;
    this.clip =
        loops
            ? RawAnimation.begin().thenLoop(animation)
            : RawAnimation.begin().thenPlayAndHold(animation);
  }

  public String animation() {
    return animation;
  }

  public RawAnimation clip() {
    return clip;
  }

  public static FamiliarPose of(
      final boolean fading,
      final long sinceSummoned,
      final boolean swinging,
      final FamiliarMode mode) {
    if (fading) {
      return FADE;
    }
    if (sinceSummoned < APPEAR_TICKS) {
      return APPEAR;
    }
    if (swinging) {
      return FIGHT;
    }
    return switch (mode) {
      case SCOUT -> SCOUT;
      case CARRY -> CARRY;
      case FIGHT -> FLOAT;
    };
  }
}
