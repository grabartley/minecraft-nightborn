package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.guild.build.HunterBuilds;
import com.grahambartley.nightborn.guild.party.HunterPartySpawner;
import com.grahambartley.nightborn.guild.party.PartyData;
import com.grahambartley.nightborn.guild.party.TentBlocks;
import com.grahambartley.nightborn.guild.raiding.RaidingFeatures;
import com.grahambartley.nightborn.guild.veteran.VeteranFeatures;

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
    VeteranFeatures.register();
    RaidingFeatures.register();
    HunterBuilds.register();
  }
}
