package com.grahambartley.nightborn.mob.household;

public final class Countdown {

  private int left;

  public Countdown(final int ticks) {
    this.left = ticks;
  }

  public boolean running() {
    return --left > 0;
  }
}
