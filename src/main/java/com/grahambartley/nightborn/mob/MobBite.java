package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.config.TurningSettings;
import com.grahambartley.nightborn.feeding.BiteRules;
import com.grahambartley.nightborn.hunter.HunterAttributes;
import com.grahambartley.nightborn.sickness.BiteSickness;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class MobBite {

  private MobBite() {}

  public static boolean bite(
      final MobEntity biter,
      final LivingEntity victim,
      final float damage,
      final int turnChance,
      final TurningSettings turning) {
    if (!(biter.getWorld() instanceof ServerWorld world)) {
      return false;
    }
    final float before = victim.getHealth();
    if (!victim.damage(NightbornDamage.bite(world, biter), damage)) {
      return false;
    }
    biter.heal(Math.max(0.0F, before - Math.max(0.0F, victim.getHealth())));
    world.playSound(
        null, biter.getBlockPos(), MobSounds.VAMPIRE_BITE, biter.getSoundCategory(), 0.8F, 0.6F);
    if (victim instanceof ServerPlayerEntity human
        && BiteRules.startsSickness(
            !Vampires.isVampire(human),
            true,
            true,
            world.getRandom().nextInt(100),
            BiteRules.resistedChance(
                turnChance, human.getAttributeValue(HunterAttributes.BITE_RESISTANCE)))) {
      BiteSickness.start(human, turning);
    }
    return true;
  }
}
