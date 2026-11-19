package com.grahambartley.nightborn.occult;

public final class OccultFeatures {

  private OccultFeatures() {}

  public static void register() {
    OccultEntities.initialize();
    OccultAttributes.register();
    OccultSounds.register();
    GhostlyFamiliars.register();
    FamiliarWisps.register();
    FamiliarModeOrders.register();
    FogClouds.register();
    RestlessDead.register();
    Hexes.register();
    MistForm.register();
    Circles.register();
    Swarms.register();
  }
}
