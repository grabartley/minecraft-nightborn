package com.grahambartley.nightborn.gore;

import java.util.Optional;

public final class GoreRules {

  private GoreRules() {}

  public record Blow(boolean bite, boolean stake, boolean silver) {}

  public static Optional<Gore> onHit(final boolean bleeds, final boolean vampire, final Blow blow) {
    if (!bleeds) {
      return Optional.empty();
    }
    if (blow.stake()) {
      return Optional.of(Gore.STAKE);
    }
    if (blow.silver() && vampire) {
      return Optional.of(Gore.SILVER);
    }
    return blow.bite() ? Optional.of(Gore.BITE) : Optional.empty();
  }

  public static Optional<Gore> onDeath(
      final boolean bleeds, final boolean vampire, final Blow blow) {
    if (!bleeds) {
      return Optional.empty();
    }
    if (vampire && blow.stake()) {
      return Optional.of(Gore.STAKED);
    }
    if (blow.bite() && !vampire) {
      return Optional.of(Gore.DRAINED);
    }
    return onHit(true, vampire, blow);
  }

  public static float sprayYaw(final double dx, final double dz) {
    if (dx == 0.0 && dz == 0.0) {
      return Float.NaN;
    }
    return (float) Math.toDegrees(Math.atan2(dz, dx));
  }
}
