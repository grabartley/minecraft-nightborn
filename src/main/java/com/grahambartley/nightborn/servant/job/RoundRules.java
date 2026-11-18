package com.grahambartley.nightborn.servant.job;

public final class RoundRules {

  public static final int STUCK_TICKS = 200;

  public enum Leg {
    CHEST,
    WORK,
    END
  }

  public enum Step {
    WORKING,
    BACK,
    NOTHING_LEFT
  }

  private RoundRules() {}

  public static Leg afterChest(
      final boolean deliveredAll, final boolean readyToWork, final boolean nothingLeft) {
    return deliveredAll && readyToWork && !nothingLeft ? Leg.WORK : Leg.END;
  }

  public static Leg afterStep(final Step step) {
    return step == Step.WORKING ? Leg.WORK : Leg.CHEST;
  }

  public static boolean stuck(final int ticksOnLeg) {
    return ticksOnLeg > STUCK_TICKS;
  }
}
