package com.grahambartley.nightborn.sight;

import java.util.Optional;
import java.util.UUID;

public record Sight(Optional<UUID> marked, float senseRange, int colour, long until) {

  public static Sight sense(final float range, final long until) {
    return new Sight(Optional.empty(), range, 0, until);
  }

  public static Sight mark(final UUID target, final int colour, final long until) {
    return new Sight(Optional.of(target), 0.0F, colour, until);
  }

  public boolean over(final long now) {
    return now >= until;
  }
}
