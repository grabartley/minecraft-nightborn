package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.guild.party.HunterPartySpawner;
import com.grahambartley.nightborn.guild.party.PartyData;
import com.grahambartley.nightborn.guild.party.TentBlocks;

public final class GuildFeatures {

  private GuildFeatures() {}

  public static void register() {
    GuildData.register();
    ContractItems.register();
    GuildMobs.register();
    TentBlocks.register();
    PartyData.register();
    HunterPartySpawner.register();
    ContractTracker.register();
    GuildWheel.register();
  }
}
