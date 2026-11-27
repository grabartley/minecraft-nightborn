package com.grahambartley.nightborn.faction;

import com.grahambartley.nightborn.guild.HunterEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public final class Hunters {

  private Hunters() {}

  public static boolean isHunter(final Entity entity) {
    return entity instanceof HunterEntity
        || entity instanceof PlayerEntity player && GuildMembership.isHunter(player);
  }
}
