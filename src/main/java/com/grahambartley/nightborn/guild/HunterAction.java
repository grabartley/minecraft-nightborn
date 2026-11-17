package com.grahambartley.nightborn.guild;

import software.bernie.geckolib.animation.RawAnimation;

public enum HunterAction {
  NONE("", false, 0, 0),
  CROSSBOW_SHOT("crossbow_shot", false, 12, 20),
  THROW_HOLY_WATER("throw_holy_water", false, 8, 16),
  RAISE_CRUCIFIX("raise_crucifix", true, 0, 0);

  static final double THROW_NEAREST = 4.0;
  static final double THROW_FARTHEST = 10.0;

  private final String animation;
  private final RawAnimation clip;
  private final int releaseTick;
  private final int lengthTicks;

  HunterAction(
      final String animation, final boolean loops, final int releaseTick, final int lengthTicks) {
    this.animation = animation;
    this.clip =
        loops ? RawAnimation.begin().thenLoop(animation) : RawAnimation.begin().thenPlay(animation);
    this.releaseTick = releaseTick;
    this.lengthTicks = lengthTicks;
  }

  public String animation() {
    return animation;
  }

  public RawAnimation clip() {
    return clip;
  }

  public int releaseTick() {
    return releaseTick;
  }

  public int lengthTicks() {
    return lengthTicks;
  }

  public boolean timed() {
    return lengthTicks > 0;
  }

  public static HunterAction byIndex(final int index) {
    final HunterAction[] actions = values();
    return index >= 0 && index < actions.length ? actions[index] : NONE;
  }

  public static HunterAction support(
      final boolean sees,
      final double distance,
      final boolean canThrow,
      final boolean hasCrucifix,
      final double wardRange) {
    if (!sees) {
      return NONE;
    }
    if (canThrow && distance >= THROW_NEAREST && distance <= THROW_FARTHEST) {
      return THROW_HOLY_WATER;
    }
    return hasCrucifix && distance <= wardRange ? RAISE_CRUCIFIX : NONE;
  }
}
