package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.gear.Staking;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public final class CoffinOwnerDeaths {

  private CoffinOwnerDeaths() {}

  public static void register() {
    ServerLivingEntityEvents.AFTER_DEATH.register(CoffinOwnerDeaths::afterDeath);
  }

  static boolean frees(final boolean sun, final boolean staked) {
    return sun || staked;
  }

  private static void afterDeath(final LivingEntity entity, final DamageSource source) {
    if (entity instanceof ServerPlayerEntity player
        && frees(source.isOf(NightbornDamage.SUN), Staking.staked(source))) {
      CoffinOwnership.release(player.server, player.getUuid());
    }
  }
}
