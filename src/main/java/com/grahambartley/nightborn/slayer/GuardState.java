package com.grahambartley.nightborn.slayer;

public record GuardState(long until, GuardPower power) {

  public boolean up(final long now) {
    return now < until;
  }

  public boolean meets(final double distance, final boolean charging) {
    return charging && distance <= power.reach();
  }
}
