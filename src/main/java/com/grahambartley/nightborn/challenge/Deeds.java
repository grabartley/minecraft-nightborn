package com.grahambartley.nightborn.challenge;

import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class Deeds {

  private Deeds() {}

  public static void record(final PlayerEntity doer, final Deed deed) {
    if (doer instanceof ServerPlayerEntity player && !player.isRemoved()) {
      record(player, deed);
    }
  }

  public static void record(final Entity doer, final Deed deed) {
    if (doer instanceof PlayerEntity player) {
      record(player, deed);
    }
  }

  public static void record(
      final MinecraftServer server, final Optional<UUID> doer, final Deed deed) {
    doer.map(id -> server.getPlayerManager().getPlayer(id))
        .ifPresent(player -> record(player, deed));
  }

  public static void record(final ServerPlayerEntity player, final Deed deed) {
    final MinecraftServer server = player.getServer();
    if (server == null) {
      return;
    }
    final ChallengeProgress.Context context = context(player);
    final ChallengeRecord before = ChallengeAttachment.get(player);
    final ChallengeProgress.Outcome outcome = ChallengeProgress.apply(before, deed, context);
    if (!outcome.record().equals(before)) {
      ChallengeAttachment.set(player, outcome.record());
    }
    ChallengeRewards.pay(player, outcome, context);
    ServerFirsts.offer(player, deed);
  }

  public static Optional<Side> side(final PlayerEntity player) {
    return Side.of(Vampires.isVampire(player), GuildMembership.isHunter(player));
  }

  static ChallengeProgress.Context context(final ServerPlayerEntity player) {
    return new ChallengeProgress.Context(
        ChallengeData.trials(player.getRegistryManager()),
        ChallengeData.feats(player.getRegistryManager()),
        side(player),
        ChallengeClock.night(player.getServer().getOverworld().getTimeOfDay()));
  }
}
