package com.grahambartley.nightborn.gear;

public final class GearFeatures {

  private GearFeatures() {}

  public static void register() {
    GearBlocks.initialize();
    GearItems.register();
    SilverOreGeneration.register();
    StakedVampires.register();
  }
}
