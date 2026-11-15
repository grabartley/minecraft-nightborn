package com.grahambartley.nightborn.skill;

public record TrackingState(long until, int range) {

  public boolean over(final long now) {
    return now >= until;
  }
}
