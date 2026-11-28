package com.grahambartley.nightborn.client.gore;

import com.grahambartley.nightborn.client.state.GoreLevel;
import net.minecraft.client.option.ParticlesMode;

public record WoundPulse(int drops, SplatterLook look) {

  static final int BEAT_TICKS = 16;
  static final int DRIP_TICKS = 40;
  static final int FULL_DROPS = 4;
  static final int DROP_BUDGET = 96;

  public static WoundPulse of(
      final GoreLevel gore, final ParticlesMode particles, final long sinceCut) {
    final SplatterLook look = SplatterLook.of(gore);
    if (!look.drawn()
        || particles == ParticlesMode.MINIMAL
        || sinceCut < 0
        || sinceCut % BEAT_TICKS != 0) {
      return new WoundPulse(0, look);
    }
    final int share = (gore == GoreLevel.FULL ? 2 : 1) * (particles == ParticlesMode.ALL ? 2 : 1);
    return new WoundPulse(Math.max(1, FULL_DROPS * share / 4), look);
  }

  public static boolean shown(final boolean invisible, final boolean ownFirstPerson) {
    return !invisible && !ownFirstPerson;
  }

  public static boolean drips(final long sinceCut) {
    return sinceCut > 0 && sinceCut % DRIP_TICKS == 0;
  }

  public static int allowed(final int spent, final int drops) {
    return Math.max(0, Math.min(drops, DROP_BUDGET - spent));
  }
}
