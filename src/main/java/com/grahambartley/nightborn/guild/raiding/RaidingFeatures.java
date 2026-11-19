package com.grahambartley.nightborn.guild.raiding;

public final class RaidingFeatures {

  private RaidingFeatures() {}

  public static void register() {
    RaidingData.register();
    RaidEnding.register();
  }
}
