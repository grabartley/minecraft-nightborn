package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.guild.build.HunterBuilds;
import com.grahambartley.nightborn.guild.party.HunterPartySpawner;
import com.grahambartley.nightborn.guild.party.LeaderlessCamps;
import com.grahambartley.nightborn.guild.party.PartyData;
import com.grahambartley.nightborn.guild.party.TentBlocks;
import com.grahambartley.nightborn.guild.party.TentPaths;
import com.grahambartley.nightborn.guild.raiding.RaidingFeatures;
import com.grahambartley.nightborn.guild.rite.RiteFeatures;
import com.grahambartley.nightborn.guild.veteran.VeteranFeatures;

public final class GuildFeatures {

  private GuildFeatures() {}

  public static void register() {
    GuildData.register();
    ContractItems.register();
    GuildMobs.register();
    TentBlocks.register();
    TentPaths.register();
    PartyData.register();
    HunterPartySpawner.register();
    LeaderlessCamps.register();
    ContractTracker.register();
    RiteFeatures.register();
    GuildWheel.register();
    VeteranFeatures.register();
    RaidingFeatures.register();
    HunterBuilds.register();
  }
}
