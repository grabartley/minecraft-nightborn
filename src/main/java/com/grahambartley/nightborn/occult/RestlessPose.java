package com.grahambartley.nightborn.occult;

import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.RawAnimation;

public enum RestlessPose {
  RISE("rise", false),
  SINK("sink", false),
  HURT("hurt", false),
  ATTACK("attack", false),
  WALK("walk", true),
  IDLE("idle", true);

  static final int RISE_TICKS = 40;
  static final int SINK_TICKS = 32;

  private final String animation;
  private final RawAnimation clip;

  RestlessPose(final String animation, final boolean loops) {
    this.animation = animation;
    this.clip =
        loops
            ? RawAnimation.begin().thenLoop(animation)
            : RawAnimation.begin().then(animation, Animation.LoopType.HOLD_ON_LAST_FRAME);
  }

  public String animation() {
    return animation;
  }

  public RawAnimation clip() {
    return clip;
  }

  public static boolean rising(final long sinceRaised) {
    return sinceRaised >= 0 && sinceRaised < RISE_TICKS;
  }

  public static RestlessPose of(
      final boolean sinking,
      final long sinceRaised,
      final boolean hurt,
      final boolean swinging,
      final boolean moving) {
    if (sinking) {
      return SINK;
    }
    if (rising(sinceRaised)) {
      return RISE;
    }
    if (hurt) {
      return HURT;
    }
    if (swinging) {
      return ATTACK;
    }
    return moving ? WALK : IDLE;
  }
}
