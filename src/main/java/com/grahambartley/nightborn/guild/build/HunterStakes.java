package com.grahambartley.nightborn.guild.build;

import com.grahambartley.nightborn.gear.Staking;
import com.grahambartley.nightborn.vampire.Vampires;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public final class HunterStakes {

  private HunterStakes() {}

  static void register() {
    ServerLivingEntityEvents.AFTER_DEATH.register(HunterStakes::afterDeath);
  }

  public static boolean pays(final boolean vampire, final boolean staked, final boolean grows) {
    return vampire && staked && grows;
  }

  private static void afterDeath(final LivingEntity entity, final DamageSource source) {
    final Entity attacker = source.getAttacker();
    if (attacker instanceof GrowingHunter hunter
        && pays(Vampires.isVampire(entity), Staking.staked(source), hunter.grows())) {
      hunter.earn(HunterBuilds.build(entity.getRegistryManager()).stakeExperience());
    }
  }
}
