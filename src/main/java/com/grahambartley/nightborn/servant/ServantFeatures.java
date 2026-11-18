package com.grahambartley.nightborn.servant;

import com.grahambartley.nightborn.servant.job.ScoutOrders;
import com.grahambartley.nightborn.structure.NearestStructures;

public final class ServantFeatures {

  private ServantFeatures() {}

  public static void register() {
    ChestMarking.register();
    NearestStructures.register();
    ScoutOrders.register();
  }
}
