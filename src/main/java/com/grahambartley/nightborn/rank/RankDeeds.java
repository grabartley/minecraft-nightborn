package com.grahambartley.nightborn.rank;

import com.grahambartley.nightborn.age.StageRanks;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.eldest.EldestRewards;
import com.grahambartley.nightborn.gear.Staking;
import com.grahambartley.nightborn.guild.ContractTracker;
import com.grahambartley.nightborn.mob.EldestEntity;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class RankDeeds {

  private RankDeeds() {}

  static void register() {
    ServerLivingEntityEvents.AFTER_DEATH.register(RankDeeds::afterDeath);
  }

  public static List<RankDeed.Act> ofStake(final boolean householdCleared) {
    final List<RankDeed.Act> acts = new ArrayList<>();
    acts.add(RankDeed.Act.STAKE);
    if (householdCleared) {
      acts.add(RankDeed.Act.CLEAR_HOUSEHOLD);
    }
    return acts;
  }

  private static void afterDeath(final LivingEntity victim, final DamageSource source) {
    final MinecraftServer server = victim.getServer();
    if (server == null || !Vampires.isVampire(victim)) {
      return;
    }
    final int stageRank = StageRanks.of(victim);
    if (Staking.staked(source)) {
      Downed.hunter(source)
          .map(id -> server.getPlayerManager().getPlayer(id))
          .ifPresent(
              hunter ->
                  Ranking.did(
                      hunter, ofStake(ContractTracker.householdCleared(victim)), stageRank));
    }
    if (victim instanceof EldestEntity eldest
        && EldestRewards.pays(Staking.staked(source), source.isOf(NightbornDamage.SUN))) {
      for (final UUID id : eldest.fighters()) {
        final ServerPlayerEntity fighter = server.getPlayerManager().getPlayer(id);
        if (fighter != null) {
          Ranking.did(fighter, List.of(RankDeed.Act.END_ELDEST), stageRank);
        }
      }
    }
  }
}
