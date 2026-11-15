package com.grahambartley.nightborn.hunter;

import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.gear.SilverOreGeneration;
import com.grahambartley.nightborn.gear.StakedVampires;

public final class HunterFeatures {

  private HunterFeatures() {}

  public static void register() {
    GearItems.register();
    SilverOreGeneration.register();
    StakedVampires.register();
    HunterAttributes.register();
    HuntRewards.register();
  }
}
