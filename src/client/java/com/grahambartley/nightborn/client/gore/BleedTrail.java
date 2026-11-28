package com.grahambartley.nightborn.client.gore;

import com.grahambartley.nightborn.client.state.GoreLevel;
import net.minecraft.client.option.ParticlesMode;

public final class BleedTrail {

  static final double FULL_SPACING = 1.0;
  static final double REDUCED_SPACING = 2.0;
  static final int SPLAT_BUDGET = 8;

  private BleedTrail() {}

  public static boolean drawn(final GoreLevel gore, final ParticlesMode particles) {
    return SplatterLook.of(gore).drawn() && particles != ParticlesMode.MINIMAL;
  }

  public static boolean affordable(final int laid) {
    return laid < SPLAT_BUDGET;
  }

  public static boolean due(
      final GoreLevel gore, final ParticlesMode particles, final double walkedSquared) {
    if (!drawn(gore, particles)) {
      return false;
    }
    final double spacing =
        (gore == GoreLevel.FULL ? FULL_SPACING : REDUCED_SPACING)
            * (particles == ParticlesMode.ALL ? 1.0 : 1.5);
    return walkedSquared >= spacing * spacing;
  }
}
