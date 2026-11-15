package com.grahambartley.nightborn.mob;

import java.util.Optional;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
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
  VOMIT("vomit", Play.ONCE),
  USE_ITEM_RIGHT("use_item_right", Play.LOOP),
  USE_ITEM_LEFT("use_item_left", Play.LOOP),
  FEED("feed", Play.LOOP),
  DRAIN("drain", Play.ONCE),
  RISE("rise", Play.ONCE),
  WEAKENED("weakened", Play.LOOP),
  WEAKENED_WALK("weakened_walk", Play.LOOP);

  static final float RUN_SWING = 0.6F;
  static final float WALK_SWING = 0.1F;
  public static final double LEVITATE_MOVE_SPEED = 0.03;
  public static final double LEVITATE_SPRINT_SPEED = 0.24;
  public static final double WEAKENED_WALK_SPEED = 0.02;
  public static final String SPREAD_VARIABLE = "variable.levitate_spread";

  private enum Play {
    LOOP,
    HOLD,
    ONCE
  }

  public record Cue(
      boolean downed,
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

  public static VampirePose weakened(final double horizontalSpeed) {
    return horizontalSpeed > WEAKENED_WALK_SPEED ? WEAKENED_WALK : WEAKENED;
  }

  public static double spread(final boolean sprinting, final double horizontalSpeed) {
    return sprinting ? 1.0 : Math.max(0.0, Math.min(1.0, horizontalSpeed / LEVITATE_SPRINT_SPEED));
  }

  public static Optional<VampirePose> action(
      final boolean swinging, final boolean using, final boolean rightArm) {
    if (using) {
      return Optional.of(rightArm ? USE_ITEM_RIGHT : USE_ITEM_LEFT);
    }
    return swinging ? Optional.of(ATTACK) : Optional.empty();
  }

  public static <T extends GeoAnimatable> PlayState playOrStop(
      final AnimationState<T> state, final Optional<VampirePose> pose) {
    if (pose.isEmpty()) {
      state.getController().forceAnimationReset();
      return PlayState.STOP;
    }
    return state.setAndContinue(pose.get().clip());
  }

  public boolean allowsActions() {
    return switch (this) {
      case VOMIT, DOWNED, RISE, FEED, DRAIN -> false;
      default -> true;
    };
  }

  public boolean holdsHead() {
    return this == DOWNED || this == RISE;
  }

  public String animation() {
    return animation;
  }

  public RawAnimation clip() {
    return clip;
  }
}
