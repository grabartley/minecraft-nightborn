package com.grahambartley.nightborn.occult;

public final class OccultFeatures {

  private OccultFeatures() {}

  public static void register() {
    OccultAttributes.register();
    OccultSounds.register();
    GhostlyFamiliars.register();
    FamiliarModeOrders.register();
    FogClouds.register();
  }
}
