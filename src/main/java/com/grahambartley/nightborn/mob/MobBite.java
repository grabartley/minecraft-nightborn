package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.grahambartley.nightborn.advancement.Milestone;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.blood.BloodValue;
import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.ward.Wards;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public final class MobBite {

  static final String SHAKEN_OFF = "nightborn.bite.shaken_off";
  static final double SHAKE_OFF_PUSH = 0.6;

  private MobBite() {}

  public static boolean bite(
      final MobEntity biter,
      final LivingEntity victim,
      final float damage,
      final SoundEvent sound) {
    if (!(biter.getWorld() instanceof ServerWorld world) || Wards.wards(victim)) {
      return false;
    }
    if (breaks(
        biter.getRandom().nextFloat(),
        GiftAttributes.value(victim, GiftAttributes.MOB_BITE_BREAK))) {
      shakeOff(world, biter, victim);
      Deeds.record(victim, Deed.of(DeedKinds.BITE_BROKEN));
      return false;
    }
    final float before = victim.getHealth();
    if (!victim.damage(NightbornDamage.bite(world, biter), damage)) {
      return false;
    }
    world.playSound(null, biter.getBlockPos(), sound, biter.getSoundCategory(), 0.8F, 0.6F);
    MilestoneCriterion.reach(victim, Milestone.BITTEN);
    if (Vampires.isVampire(biter)) {
      Deeds.record(victim, Deed.of(DeedKinds.BITTEN));
    }
    if (Prey.hasDrinkableBlood(victim)) {
      biter.heal(BloodValue.taken(before, victim.getHealth()));
      if (Vampires.isVampire(biter)) {
        FeedingSessions.fed(biter, world.getTime());
      }
    }
    return true;
  }

  static boolean breaks(final float roll, final double chance) {
    return roll < chance;
  }

  private static void shakeOff(
      final ServerWorld world, final MobEntity biter, final LivingEntity victim) {
    biter.takeKnockback(SHAKE_OFF_PUSH, victim.getX() - biter.getX(), victim.getZ() - biter.getZ());
    biter.velocityModified = true;
    world.playSound(
        null,
        victim.getBlockPos(),
        SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK,
        SoundCategory.PLAYERS,
        1.0F,
        0.8F);
    if (victim instanceof PlayerEntity player) {
      player.sendMessage(Text.translatable(SHAKEN_OFF), true);
    }
  }
}
