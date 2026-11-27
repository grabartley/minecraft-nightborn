package com.grahambartley.nightborn.guild.rite;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

public final class RiteFeatures {

  private RiteFeatures() {}

  public static void register() {
    DynamicRegistries.register(GuildRite.KEY, GuildRite.CODEC);
    RiteCooldownAttachment.register();
    Sanctuary.register();
    GuildRites.register();
  }
}
