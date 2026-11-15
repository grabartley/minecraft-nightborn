package com.grahambartley.nightborn.client.vomit;

import com.grahambartley.nightborn.client.state.GoreLevel;

public record VomitBurst(int drops, boolean splashes) {

  static final VomitBurst FULL = new VomitBurst(14, true);
  static final VomitBurst REDUCED = new VomitBurst(5, false);
  static final VomitBurst NONE = new VomitBurst(0, false);

  public static VomitBurst of(final GoreLevel gore) {
    return switch (gore) {
      case FULL -> FULL;
      case REDUCED -> REDUCED;
      case OFF -> NONE;
    };
  }
}
