package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.ammo.AmmoEntities;
import com.grahambartley.nightborn.ammo.AmmoItems;
import com.grahambartley.nightborn.ammo.GarlicFumesEffect;
import com.grahambartley.nightborn.coating.Coatings;
import com.grahambartley.nightborn.holy.HolyWater;
import com.grahambartley.nightborn.skill.StakedVampires;
import com.grahambartley.nightborn.sun.ParasolEvents;
import com.grahambartley.nightborn.sun.Parasols;
import com.grahambartley.nightborn.ward.WardPoints;

public final class GearFeatures {

  private GearFeatures() {}

  public static void register() {
    GearBlocks.initialize();
    GearItems.register();
    Parasols.register();
    ParasolEvents.register();
    SilverOreGeneration.register();
    GarlicGeneration.register();
    WardPoints.register();
    StakedVampires.register();
    HolyWater.register();
    GarlicFumesEffect.register();
    AmmoEntities.initialize();
    AmmoItems.register();
    Coatings.register();
    BaneHitSounds.register();
    Banes.register();
  }
}
