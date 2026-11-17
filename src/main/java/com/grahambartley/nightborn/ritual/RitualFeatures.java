package com.grahambartley.nightborn.ritual;

import com.grahambartley.nightborn.eldest.EldestRewards;

public final class RitualFeatures {

  private RitualFeatures() {}

  public static void register() {
    RitualSounds.initialize();
    RitualItems.register();
    RitualBlocks.register();
    EldestRewards.register();
  }
}
