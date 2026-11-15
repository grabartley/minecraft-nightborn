package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.config.TurningSettings;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.sickness.BiteSickness;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.ward.Wards;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;

public final class MobBite {

  private MobBite() {}

  public static boolean bite(
      final MobEntity biter,
      final LivingEntity victim,
      final float damage,
      final int turnChance,
      final SoundEvent sound,
      final TurningSettings turning) {
    if (!(biter.getWorld() instanceof ServerWorld world) || Wards.wards(victim)) {
      return false;
    }
    final float before = victim.getHealth();
    if (!victim.damage(NightbornDamage.bite(world, biter), damage)) {
      return false;
    }
    world.playSound(null, biter.getBlockPos(), sound, biter.getSoundCategory(), 0.8F, 0.6F);
    if (Prey.hasDrinkableBlood(victim)) {
      biter.heal(Math.max(0.0F, before - Math.max(0.0F, victim.getHealth())));
      if (Vampires.isVampire(biter)) {
        FeedingSessions.fed(biter, world.getTime());
      }
    }
    BiteSickness.startFromBite(victim, turnChance, true, true, world.getRandom(), turning);
    return true;
  }
}
