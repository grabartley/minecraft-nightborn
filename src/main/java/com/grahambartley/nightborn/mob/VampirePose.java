package com.grahambartley.nightborn.mob;

import java.util.Optional;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

public enum VampirePose {
  IDLE("idle", Play.LOOP),
  IDLE_LOOK("idle_look", Play.ONCE, Hold.HEAD),
  IDLE_LISTEN("idle_listen", Play.ONCE, Hold.HEAD),
  WALK("walk", Play.LOOP),
  RUN("run", Play.LOOP),
  ATTACK("attack", Play.HOLD),
  DOWNED("downed", Play.HOLD, Hold.ARMS_AND_HEAD),
  LEVITATE("levitate", Play.LOOP),
  LEVITATE_MOVE("levitate_move", Play.LOOP),
  LEVITATE_SPRINT("levitate_sprint", Play.LOOP),
  LAND("land", Play.ONCE),
  VOMIT("vomit", Play.ONCE, Hold.ARMS),
  USE_ITEM_RIGHT("use_item_right", Play.LOOP),
  USE_ITEM_LEFT("use_item_left", Play.LOOP),
  FEED("feed", Play.LOOP, Hold.ARMS_AND_HEAD),
  DRAIN("drain", Play.ONCE, Hold.ARMS_AND_HEAD),
  RISE("rise", Play.ONCE, Hold.ARMS_AND_HEAD),
  WEAKENED("weakened", Play.LOOP),
  WEAKENED_WALK("weakened_walk", Play.LOOP),
  COFFIN_SLEEP("coffin_sleep", Play.HOLD, Hold.ARMS_HEAD_AND_HANDS),
  COFFIN_RISE("coffin_rise", Play.HOLD, Hold.ARMS_HEAD_AND_HANDS),
  COFFIN_STEP_OUT("coffin_step_out", Play.HOLD, Hold.ARMS_HEAD_AND_HANDS),
  SIT("sit", Play.LOOP),
  READ("read", Play.LOOP, Hold.ARMS_AND_HEAD);

  public static final double LEVITATE_MOVE_SPEED = 0.03;
  public static final double LEVITATE_SPRINT_SPEED = 0.24;
  public static final double WEAKENED_WALK_SPEED = 0.02;
  public static final String SPREAD_VARIABLE = "variable.levitate_spread";

  private enum Play {
    LOOP,
    HOLD,
    ONCE
  }

  private enum Hold {
    NOTHING,
    HEAD,
    ARMS,
    ARMS_AND_HEAD,
    ARMS_HEAD_AND_HANDS
  }

  public record Cue(
      Optional<VampirePose> coffin,
      boolean downed,
      boolean levitating,
      boolean landing,
      boolean sprinting,
      float limbSwing,
      double horizontalSpeed,
      boolean seated,
      boolean reading) {}

  private final String animation;
  private final RawAnimation clip;
  private final Hold hold;

  VampirePose(final String animation, final Play play) {
    this(animation, play, Hold.NOTHING);
  }

  VampirePose(final String animation, final Play play, final Hold hold) {
    this.animation = animation;
    this.hold = hold;
    this.clip =
        switch (play) {
          case LOOP -> RawAnimation.begin().thenLoop(animation);
          case HOLD -> RawAnimation.begin().thenPlayAndHold(animation);
          case ONCE -> RawAnimation.begin().thenPlay(animation);
        };
  }

  public static VampirePose of(final Cue cue) {
    if (cue.coffin().isPresent()) {
      return cue.coffin().get();
    }
    if (cue.downed()) {
      return DOWNED;
    }
    if (cue.levitating()) {
      return levitating(cue.sprinting(), cue.horizontalSpeed());
    }
    if (cue.landing()) {
      return LAND;
    }
    if (cue.seated()) {
      return SIT;
    }
    return switch (Gait.of(cue.limbSwing())) {
      case RUN -> RUN;
      case WALK -> WALK;
      case STILL -> cue.reading() ? READ : IDLE;
    };
  }

  public static VampirePose coffin(final boolean rising, final boolean standingCoffin) {
    if (!rising) {
      return COFFIN_SLEEP;
    }
    return standingCoffin ? COFFIN_STEP_OUT : COFFIN_RISE;
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
    return hold == Hold.NOTHING || hold == Hold.HEAD;
  }

  public boolean holdsHead() {
    return hold == Hold.HEAD || hold == Hold.ARMS_AND_HEAD || hold == Hold.ARMS_HEAD_AND_HANDS;
  }

  public boolean showsHeldItems() {
    return hold != Hold.ARMS_HEAD_AND_HANDS;
  }

  public String animation() {
    return animation;
  }

  public RawAnimation clip() {
    return clip;
  }
}
