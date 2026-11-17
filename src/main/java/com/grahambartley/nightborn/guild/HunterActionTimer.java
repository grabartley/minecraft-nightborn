package com.grahambartley.nightborn.guild;

public final class HunterActionTimer {

  public enum Step {
    IDLE,
    WAIT,
    RELEASE,
    DONE
  }

  private HunterAction action = HunterAction.NONE;
  private int age;

  public boolean begin(final HunterAction next) {
    if (busy() || !next.timed()) {
      return false;
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

  public Step tick() {
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
