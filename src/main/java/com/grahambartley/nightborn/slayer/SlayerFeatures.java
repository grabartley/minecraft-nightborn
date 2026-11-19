package com.grahambartley.nightborn.slayer;

public final class SlayerFeatures {

  private SlayerFeatures() {}

  public static void register() {
    SlayerEntities.initialize();
    SlayerMarks.register();
    Charges.register();
    Dashes.register();
    Stands.register();
    SlayerBlows.register();
  }
}
