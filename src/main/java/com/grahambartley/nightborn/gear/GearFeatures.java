package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.skill.StakedVampires;
import com.grahambartley.nightborn.ward.WardPoints;

public final class GearFeatures {

  private GearFeatures() {}

  public static void register() {
    GearBlocks.initialize();
    GearItems.register();
    SilverOreGeneration.register();
    GarlicGeneration.register();
    WardPoints.register();
    StakedVampires.register();
  }
}
