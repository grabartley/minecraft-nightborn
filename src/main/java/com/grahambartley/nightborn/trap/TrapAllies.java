package com.grahambartley.nightborn.trap;

import com.grahambartley.nightborn.faction.Hunters;
import net.minecraft.entity.LivingEntity;

public final class TrapAllies {

  private TrapAllies() {}

  public static boolean allied(final LivingEntity owner, final LivingEntity other) {
    return TrapRules.allied(
        owner.getUuid().equals(other.getUuid()),
        Hunters.isHunter(owner),
        Hunters.isHunter(other),
        owner.isTeammate(other));
  }
}
