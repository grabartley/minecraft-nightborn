package com.grahambartley.nightborn.slayer;

public record DashState(long startedAt, DashPower power) {

  public boolean untouchable(final long now) {
    return now - startedAt < power.untouchableTicks();
  }

  public boolean dodges(final long now) {
    return now - startedAt <= power.dodgeTicks();
  }

  public boolean over(final long now) {
    return !untouchable(now) && !dodges(now);
  }
}
