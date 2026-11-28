package com.grahambartley.nightborn.guild;

public final class HunterActionTimer {

  public enum Step {
    IDLE,
    WAIT,
    RELEASE,
    DONE
  }

  public static final int THROW_COOLDOWN_TICKS = 160;

  private HunterAction action = HunterAction.NONE;
  private int age;
  private int throwCooldown;

  public boolean begin(final HunterAction next) {
    if (busy() || !next.timed()) {
      return false;
    }
    if (next == HunterAction.THROW_HOLY_WATER) {
      if (throwCooldown > 0) {
        return false;
      }
      throwCooldown = THROW_COOLDOWN_TICKS;
    }
    action = next;
    age = 0;
    return true;
  }

  public boolean busy() {
    return action.timed();
  }

  public HunterAction action() {
    return action;
  }

  public boolean canThrow() {
    return throwCooldown == 0;
  }

  public Step tick() {
    if (throwCooldown > 0) {
      throwCooldown--;
    }
    if (!busy()) {
      return Step.IDLE;
    }
    age++;
    if (age >= action.lengthTicks()) {
      action = HunterAction.NONE;
      return Step.DONE;
    }
    return age == action.releaseTick() ? Step.RELEASE : Step.WAIT;
  }
}
