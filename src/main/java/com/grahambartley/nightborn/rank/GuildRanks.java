package com.grahambartley.nightborn.rank;

import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.faction.OathAttachment;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;

public final class GuildRanks {

  private GuildRanks() {}

  public static int of(final AttachmentTarget player) {
    return GuildMembership.isHunter(player) ? OathAttachment.get(player).rank() : 0;
  }
}
