package com.grahambartley.nightborn.seeker;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record Seeing(long until, SightPower power, Set<UUID> told) {

  public static Seeing of(final long now, final SightPower power) {
    return new Seeing(now + power.durationTicks(), power, new HashSet<>());
  }

  public boolean lasts(final long now) {
    return now < until;
  }

  public boolean firstSight(final UUID vampire) {
    return told.add(vampire);
  }
}
