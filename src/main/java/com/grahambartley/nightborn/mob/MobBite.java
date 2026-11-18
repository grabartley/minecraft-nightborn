package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.advancement.Milestone;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.blood.BloodValue;
import com.grahambartley.nightborn.feeding.FeedingSessions;
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
      final SoundEvent sound) {
    if (!(biter.getWorld() instanceof ServerWorld world) || Wards.wards(victim)) {
      return false;
    }
    final float before = victim.getHealth();
    if (!victim.damage(NightbornDamage.bite(world, biter), damage)) {
      return false;
    }
    world.playSound(null, biter.getBlockPos(), sound, biter.getSoundCategory(), 0.8F, 0.6F);
    MilestoneCriterion.reach(victim, Milestone.BITTEN);
    if (Prey.hasDrinkableBlood(victim)) {
      biter.heal(BloodValue.taken(before, victim.getHealth()));
      if (Vampires.isVampire(biter)) {
        FeedingSessions.fed(biter, world.getTime());
      }
    }
    return true;
  }
}
