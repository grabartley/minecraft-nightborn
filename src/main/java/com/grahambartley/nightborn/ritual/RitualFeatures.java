package com.grahambartley.nightborn.ritual;

import com.grahambartley.nightborn.boss.BossEntities;
import com.grahambartley.nightborn.boss.BossFights;
import com.grahambartley.nightborn.eldest.EldestRewards;
import com.grahambartley.nightborn.eldest.EldestSet;
import com.grahambartley.nightborn.eldest.EldestSpoils;

public final class RitualFeatures {

  private RitualFeatures() {}

  public static void register() {
    BossFights.register();
    BossEntities.initialize();
    RitualSounds.initialize();
    RitualItems.register();
    RitualBlocks.register();
    EldestRewards.register();
    EldestSpoils.register();
    EldestSet.register();
  }
}
