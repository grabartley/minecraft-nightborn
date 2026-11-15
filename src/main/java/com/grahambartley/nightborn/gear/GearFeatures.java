package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.skill.StakedVampires;

public final class GearFeatures {

  private GearFeatures() {}

  public static void register() {
    GearBlocks.initialize();
    GearItems.register();
    SilverOreGeneration.register();
    StakedVampires.register();
  }
}
