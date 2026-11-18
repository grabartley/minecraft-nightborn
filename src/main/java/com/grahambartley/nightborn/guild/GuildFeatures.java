package com.grahambartley.nightborn.guild;

public final class GuildFeatures {

  private GuildFeatures() {}

  public static void register() {
    GuildData.register();
    ContractItems.register();
    GuildMobs.register();
    ContractTracker.register();
    GuildWheel.register();
  }
}
