package com.grahambartley.nightborn.mob;

import software.bernie.geckolib.animation.RawAnimation;

public enum VampirePose {
  IDLE("idle", Play.LOOP),
  WALK("walk", Play.LOOP),
  RUN("run", Play.LOOP),
  ATTACK("attack", Play.HOLD),
  DOWNED("downed", Play.HOLD),
  LEVITATE("levitate", Play.LOOP),
  LEVITATE_MOVE("levitate_move", Play.LOOP),
  LEVITATE_SPRINT("levitate_sprint", Play.LOOP),
  LAND("land", Play.ONCE),
  VOMIT("vomit", Play.ONCE);

  static final float RUN_SWING = 0.6F;
  static final float WALK_SWING = 0.1F;
  public static final double LEVITATE_MOVE_SPEED = 0.03;
  public static final double LEVITATE_SPRINT_SPEED = 0.24;

  private enum Play {
    LOOP,
    HOLD,
    ONCE
  }

  public record Cue(
      boolean downed,
      boolean attacking,
      boolean levitating,
      boolean landing,
      boolean sprinting,
      float limbSwing,
      double horizontalSpeed) {}

  private final String animation;
  private final RawAnimation clip;

  VampirePose(final String animation, final Play play) {
    this.animation = animation;
    this.clip =
        switch (play) {
          case LOOP -> RawAnimation.begin().thenLoop(animation);
          case HOLD -> RawAnimation.begin().thenPlayAndHold(animation);
          case ONCE -> RawAnimation.begin().thenPlay(animation);
        };
  }

  public static VampirePose of(final Cue cue) {
    if (cue.downed()) {
      return DOWNED;
    }
    if (cue.attacking()) {
      return ATTACK;
    }
    if (cue.levitating()) {
      return levitating(cue.sprinting(), cue.horizontalSpeed());
    }
    if (cue.landing()) {
      return LAND;
    }
    if (cue.limbSwing() > RUN_SWING) {
      return RUN;
    }
    return cue.limbSwing() > WALK_SWING ? WALK : IDLE;
  }

  public static VampirePose levitating(final boolean sprinting, final double horizontalSpeed) {
    if (sprinting || horizontalSpeed > LEVITATE_SPRINT_SPEED) {
      return LEVITATE_SPRINT;
    }
    return horizontalSpeed > LEVITATE_MOVE_SPEED ? LEVITATE_MOVE : LEVITATE;
  }

  public String animation() {
    return animation;
  }

  public RawAnimation clip() {
    return clip;
  }
}
