package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.sun.DirectSun;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

final class Diagnostics {

  private Diagnostics() {}

  static String of(final ServerPlayerEntity player) {
    final DamageSource last = player.getRecentDamageSource();
    return "health "
        + player.getHealth()
        + "/"
        + player.getMaxHealth()
        + ", blood "
        + BloodAttachment.get(player)
        + ", last hurt by "
        + (last == null ? "nothing" : last.getName())
        + ", in sun "
        + DirectSun.on(player)
        + ", day "
        + player.getWorld().isDay()
        + ", time "
        + player.getWorld().getTimeOfDay()
        + ", sky "
        + player.getWorld().isSkyVisible(player.getBlockPos().up())
        + ", fire resistant "
        + player.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)
        + ", pos "
        + player.getBlockPos().toShortString()
        + ", sneaking "
        + player.isSneaking()
        + ", age "
        + player.age;
  }
}
