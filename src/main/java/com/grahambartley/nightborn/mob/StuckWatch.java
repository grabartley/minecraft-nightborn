package com.grahambartley.nightborn.mob;

public record StuckWatch(double best, int stalled, int reroutes) {

  public static final StuckWatch FRESH = new StuckWatch(Double.NaN, 0, 0);

  static final double CLOSE = 3.0;
  static final double PROGRESS = 0.5;
  static final int GIVE_UP_REROUTES = 3;

  public enum Step {
    NONE,
    LAND,
    STEP_BACK,
    GIVE_UP
  }

  public StuckWatch next(final double distance, final boolean pursuing) {
    if (!pursuing || Double.isNaN(distance) || distance <= CLOSE) {
      return FRESH;
    }
    if (Double.isNaN(best) || distance < best - PROGRESS) {
      return new StuckWatch(distance, 0, 0);
    }
    return new StuckWatch(best, stalled + 1, reroutes);
  }

  public boolean stuck(final int patience) {
    return stalled >= patience;
  }

  public Step step(final int patience) {
    if (stalled == patience) {
      return Step.LAND;
    }
    if (stalled == patience * 2) {
      return reroutes + 1 >= GIVE_UP_REROUTES ? Step.GIVE_UP : Step.STEP_BACK;
    }
    return Step.NONE;
  }

  public StuckWatch rerouted() {
    return new StuckWatch(best, 0, reroutes + 1);
  }
}
