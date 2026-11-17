package com.grahambartley.nightborn.mind;

import com.grahambartley.nightborn.thrall.Thralls;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.DamageTypeTags;

public final class MindFeatures {

  private MindFeatures() {}

  public static void register() {
    MindAttributes.register();
    MindEffect.register();
    MindTies.register();
    Thralls.register();
    Gazes.register();
    ServerLivingEntityEvents.ALLOW_DAMAGE.register(MindFeatures::allowDamage);
    UseEntityCallback.EVENT.register(Charm::onUse);
  }

  public static boolean stilled(final LivingEntity entity) {
    return entity instanceof MobEntity && MindTies.mesmerised(entity);
  }

  public static boolean hold(final MobEntity mob) {
    if (!stilled(mob)) {
      return false;
    }
    mob.setForwardSpeed(0.0F);
    mob.setSidewaysSpeed(0.0F);
    mob.setUpwardSpeed(0.0F);
    mob.setJumping(false);
    return true;
  }

  static boolean allowDamage(
      final LivingEntity victim, final DamageSource source, final float amount) {
    return !(source.getAttacker() instanceof LivingEntity attacker)
        || !MindGuard.blocks(
            MindTies.mesmerised(attacker),
            attacker instanceof PlayerEntity,
            MindTies.mesmerisedBy(attacker, victim),
            source.isIn(DamageTypeTags.IS_EXPLOSION));
  }
}
