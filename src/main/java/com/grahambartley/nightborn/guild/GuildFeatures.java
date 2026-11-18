package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.guild.party.HunterPartySpawner;
import com.grahambartley.nightborn.guild.party.PartyData;

public final class GuildFeatures {

  private GuildFeatures() {}

  public static void register() {
    GuildData.register();
    ContractItems.register();
    GuildMobs.register();
    PartyData.register();
    HunterPartySpawner.register();
    ContractTracker.register();
  }
}
