package com.grahambartley.nightborn.challenge;

import com.grahambartley.nightborn.age.StageRanks;
import com.grahambartley.nightborn.coffin.CoffinSleepers;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.eldest.EldestRewards;
import com.grahambartley.nightborn.gear.Staking;
import com.grahambartley.nightborn.guild.ContractTracker;
import com.grahambartley.nightborn.house.Houses;
import com.grahambartley.nightborn.mob.EldestEntity;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.VampireLordEntity;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.village.VillageSuspicion;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class ChallengeHooks {

  private ChallengeHooks() {}

  static void register() {
    ServerLivingEntityEvents.AFTER_DEATH.register(ChallengeHooks::afterDeath);
  }

  private static void afterDeath(final LivingEntity victim, final DamageSource source) {
    final MinecraftServer server = victim.getServer();
    if (server == null || !(victim.getWorld() instanceof ServerWorld world)) {
      return;
    }
    if (victim.getType() == NightbornMobs.VAMPIRE_BAT) {
      Deeds.record(source.getAttacker(), Deed.of(DeedKinds.VAMPIRE_BAT_KILLED));
      return;
    }
    if (!Vampires.isVampire(victim)) {
      return;
    }
    final boolean staked = Staking.staked(source);
    if (staked && victim instanceof ServerPlayerEntity player) {
      Deeds.record(player, Deed.of(DeedKinds.STAKED));
    }
    final Optional<UUID> staker = staked ? Downed.hunter(source) : Optional.empty();
    if (victim instanceof EldestEntity eldest) {
      if (EldestRewards.pays(staked, source.isOf(NightbornDamage.SUN))) {
        eldestEnded(server, eldest, staker);
      }
      return;
    }
    staker
        .map(id -> server.getPlayerManager().getPlayer(id))
        .ifPresentOrElse(
            hunter -> vampireStaked(world, hunter, victim),
            () -> sunFinished(server, victim, source));
  }

  private static void eldestEnded(
      final MinecraftServer server, final EldestEntity eldest, final Optional<UUID> staker) {
    for (final UUID id : eldest.fighters()) {
      final ServerPlayerEntity fighter = server.getPlayerManager().getPlayer(id);
      if (fighter != null) {
        Deeds.record(
            fighter,
            Deed.of(DeedKinds.ELDEST_ENDED)
                .with(DeedKinds.STAKER, staker.filter(id::equals).isPresent())
                .with(DeedKinds.SUMMONER, eldest.summonedBy(fighter))
                .with(DeedKinds.ARMOURED, DeedFacts.armoured(fighter)));
      }
    }
  }

  private static void vampireStaked(
      final ServerWorld world, final ServerPlayerEntity hunter, final LivingEntity victim) {
    Deeds.record(
        hunter,
        Deed.of(DeedKinds.VAMPIRE_STAKED)
            .with(DeedKinds.STAGE_RANK, StageRanks.of(victim))
            .with(DeedKinds.IN_VILLAGE, VillageSuspicion.bellFor(world, victim).isPresent())
            .with(DeedKinds.IN_COFFIN, CoffinSleepers.inCoffin(victim))
            .with(DeedKinds.LORD, lord(world, victim)));
    if (ContractTracker.householdCleared(victim)) {
      Deeds.record(
          hunter,
          Deed.of(DeedKinds.HOUSEHOLD_CLEARED)
              .with(DeedKinds.CASTLE, Castles.contains(world, home(victim)))
              .with(DeedKinds.BY_DAY, DeedFacts.byDay(hunter))
              .with(DeedKinds.NEAREST_PLAYER, DeedFacts.nearestOtherPlayer(hunter)));
    }
  }

  private static void sunFinished(
      final MinecraftServer server, final LivingEntity victim, final DamageSource source) {
    final DownedState state = Downed.state(victim);
    if (state != null && source.isOf(NightbornDamage.SUN)) {
      Deeds.record(server, state.downedBy(), Deed.of(DeedKinds.SUN_FINISHED));
    }
  }

  static boolean lord(final ServerWorld world, final LivingEntity victim) {
    if (victim instanceof VampireLordEntity) {
      return true;
    }
    if (victim instanceof VampireEntity vampire) {
      return vampire.householdTie().head();
    }
    return victim instanceof ServerPlayerEntity
        && Houses.get(world.getServer()).ledBy(victim.getUuid()).isPresent();
  }

  private static BlockPos home(final LivingEntity victim) {
    return victim instanceof VampireEntity vampire
        ? vampire.homeLife().home().orElse(victim.getBlockPos())
        : victim.getBlockPos();
  }
}
