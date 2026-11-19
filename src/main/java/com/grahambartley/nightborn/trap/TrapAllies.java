package com.grahambartley.nightborn.trap;

import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.guild.HunterEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class TrapAllies {

  private TrapAllies() {}

  public static boolean allied(final LivingEntity owner, final LivingEntity other) {
    return TrapRules.allied(
        owner.getUuid().equals(other.getUuid()),
        hunter(owner),
        hunter(other),
        owner.isTeammate(other));
  }

  static boolean hunter(final LivingEntity entity) {
    return entity instanceof HunterEntity
        || entity instanceof PlayerEntity player && GuildMembership.isHunter(player);
  }
}
