package com.grahambartley.nightborn.hunter;

public record TrackingState(long until, int range) {

  public boolean over(final long now) {
    return now >= until;
  }
}
