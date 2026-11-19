package com.grahambartley.nightborn.skill;

public record TrackingState(long until, int range, boolean bloodTrails) {

  public boolean over(final long now) {
    return now >= until;
  }
}
