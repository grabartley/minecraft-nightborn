package com.grahambartley.nightborn.client.gore;

import com.grahambartley.nightborn.client.state.GoreLevel;
import com.grahambartley.nightborn.gore.Gore;
import net.minecraft.client.option.ParticlesMode;

public record BloodBurst(
    int drops,
    double height,
    double speed,
    double spread,
    boolean allAround,
    int pools,
    int smoke,
    SplatterLook look) {

  static final BloodBurst NONE = new BloodBurst(0, 0.0, 0.0, 0.0, false, 0, 0, SplatterLook.HIDDEN);

  public static BloodBurst of(
      final GoreLevel gore, final ParticlesMode particles, final Gore kind) {
    final SplatterLook look = SplatterLook.of(gore);
    if (!look.drawn() || particles == ParticlesMode.MINIMAL) {
      return NONE;
    }
    final BloodBurst full = full(kind, look);
    final int share = (gore == GoreLevel.FULL ? 2 : 1) * (particles == ParticlesMode.ALL ? 2 : 1);
    return new BloodBurst(
        Math.max(1, full.drops * share / 4),
        full.height,
        full.speed,
        full.spread,
        full.allAround,
        full.pools,
        full.smoke * share / 4,
        look);
  }

  private static BloodBurst full(final Gore kind, final SplatterLook look) {
    return switch (kind) {
      case BITE -> new BloodBurst(8, 0.85, 0.12, 0.05, false, 0, 0, look);
      case STAKE -> new BloodBurst(18, 0.6, 0.22, 0.08, false, 0, 0, look);
      case SILVER -> new BloodBurst(10, 0.6, 0.16, 0.06, false, 0, 0, look);
      case DRAINED -> new BloodBurst(14, 0.85, 0.03, 0.03, false, 1, 0, look);
      case STAKED -> new BloodBurst(32, 0.6, 0.24, 0.1, true, 1, 12, look);
    };
  }
}
