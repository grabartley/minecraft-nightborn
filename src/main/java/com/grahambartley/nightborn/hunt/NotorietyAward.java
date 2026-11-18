package com.grahambartley.nightborn.hunt;

import com.grahambartley.nightborn.age.AgeAttachment;
import com.grahambartley.nightborn.age.AgeLadder;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.servant.Servants;
import com.grahambartley.nightborn.servant.job.DayWorker;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class NotorietyAward {

  private NotorietyAward() {}

  static void register() {
    ServerLivingEntityEvents.AFTER_DEATH.register(NotorietyAward::afterDeath);
  }

  public static Optional<UUID> credited(
      final Optional<UUID> playerKiller, final Optional<UUID> killersMaster) {
    return playerKiller.isPresent() ? playerKiller : killersMaster;
  }

  private static void afterDeath(final LivingEntity victim, final DamageSource source) {
    final MinecraftServer server = victim.getServer();
    if (server == null || victim instanceof PlayerEntity || StandIn.is(victim)) {
      return;
    }
    final List<NotorietyKill> kills = HuntData.kills(server.getRegistryManager());
    if (kills.stream().noneMatch(kill -> victim.getType().isIn(kill.victims()))) {
      return;
    }
    final ServerPlayerEntity vampire = vampire(server, source.getAttacker());
    if (vampire == null) {
      return;
    }
    final double multiplier =
        AgeLadder.of(server.getRegistryManager()).notoriety(AgeAttachment.get(vampire).rank());
    Hunts.add(
        vampire,
        NotorietyKill.worth(kills, victim.getType()::isIn, BandCheck.last(victim), multiplier));
  }

  static Optional<UUID> master(final Entity killer) {
    return Servants.masterOf(killer)
        .or(() -> killer instanceof DayWorker worker ? worker.owner() : Optional.empty())
        .or(
            () ->
                killer instanceof VampireEntity member
                    ? member.householdTie().lord()
                    : Optional.empty());
  }

  @Nullable
  private static ServerPlayerEntity vampire(
      final MinecraftServer server, @Nullable final Entity killer) {
    if (killer == null) {
      return null;
    }
    return credited(
            killer instanceof ServerPlayerEntity ? Optional.of(killer.getUuid()) : Optional.empty(),
            master(killer))
        .map(id -> server.getPlayerManager().getPlayer(id))
        .filter(Vampires::isVampire)
        .orElse(null);
  }
}
