package com.grahambartley.nightborn.client.vomit;

import com.grahambartley.nightborn.client.state.GoreLevel;
import net.minecraft.client.option.ParticlesMode;

public record VomitBurst(int drops, boolean splashes) {

  static final VomitBurst FULL = new VomitBurst(14, true);
  static final VomitBurst REDUCED = new VomitBurst(5, false);
  static final VomitBurst NONE = new VomitBurst(0, false);

  public static VomitBurst of(final GoreLevel gore, final ParticlesMode particles) {
    if (gore == GoreLevel.OFF || particles == ParticlesMode.MINIMAL) {
      return NONE;
    }
    return gore == GoreLevel.FULL && particles == ParticlesMode.ALL ? FULL : REDUCED;
  }
}
