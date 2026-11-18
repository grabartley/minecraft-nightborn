package com.grahambartley.nightborn.raid;

public final class RaidFeatures {

  private RaidFeatures() {}

  public static void register() {
    RaidData.register();
    HouseNoise.register();
    NoiseSources.register();
    Trails.register();
  }
}
