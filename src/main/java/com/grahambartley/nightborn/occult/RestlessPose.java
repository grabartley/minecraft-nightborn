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

  public static final long NEVER = -1L;
  static final int RISE_TICKS = 40;
  static final int SINK_TICKS = 32;
  static final int ATTACK_TICKS = 12;

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

  public int blendTicks(final int usual) {
    return this == RISE ? 0 : usual;
  }

  public static long since(final long at, final long now) {
    return at < 0 ? NEVER : now - at;
  }

  public static boolean rising(final long sinceRaised) {
    return sinceRaised >= 0 && sinceRaised < RISE_TICKS;
  }

  public static boolean striking(final long sinceStruck) {
    return sinceStruck >= 0 && sinceStruck < ATTACK_TICKS;
  }

  public static boolean held(final boolean sinking, final long sinceRaised) {
    return sinking || rising(sinceRaised);
  }

  public static RestlessPose of(
      final boolean sinking,
      final long sinceRaised,
      final boolean hurt,
      final long sinceStruck,
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
    if (striking(sinceStruck)) {
      return ATTACK;
    }
    return moving ? WALK : IDLE;
  }
}
