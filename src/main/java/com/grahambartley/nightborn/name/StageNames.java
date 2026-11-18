package com.grahambartley.nightborn.name;

import com.grahambartley.nightborn.age.AgeAttachment;
import com.grahambartley.nightborn.age.AgeLadder;
import com.grahambartley.nightborn.challenge.ChallengeAttachment;
import com.grahambartley.nightborn.challenge.Titles;
import com.grahambartley.nightborn.faction.FactionPlayerList;
import com.grahambartley.nightborn.faction.OathAttachment;
import com.grahambartley.nightborn.house.HouseNames;
import com.grahambartley.nightborn.network.StageNamePayload;
import com.grahambartley.nightborn.rank.RankLadder;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class StageNames {

  private StageNames() {}

  public static void register() {
    EntityTrackingEvents.START_TRACKING.register(
        (tracked, viewer) -> {
          if (tracked instanceof ServerPlayerEntity bearer) {
            send(viewer, bearer);
          }
        });
    ServerConfigService.onChange(
        (server, config) ->
            server.getPlayerManager().getPlayerList().forEach(StageNames::announce));
  }

  public static Optional<Identifier> shown(
      final Optional<Identifier> stage,
      final boolean bearerVampire,
      final boolean viewerVampire,
      final boolean factionsShown) {
    return bearerVampire && (viewerVampire || factionsShown) ? stage : Optional.empty();
  }

  public static void changed(final ServerPlayerEntity player) {
    announce(player);
    for (final ServerPlayerEntity other : player.getServerWorld().getPlayers()) {
      if (other != player && PlayerLookup.tracking(other).contains(player)) {
        send(player, other);
      }
    }
  }

  public static void announce(final ServerPlayerEntity bearer) {
    PlayerLookup.tracking(bearer).forEach(viewer -> send(viewer, bearer));
  }

  private static void send(final ServerPlayerEntity viewer, final ServerPlayerEntity bearer) {
    final Optional<Identifier> stage =
        AgeLadder.of(bearer.getRegistryManager())
            .rung(AgeAttachment.get(bearer).rank())
            .map(AgeLadder.Rung::id);
    final Optional<Identifier> shownStage =
        shown(
            stage,
            Vampires.isVampire(bearer),
            Vampires.isVampire(viewer),
            FactionPlayerList.shown());
    ServerPlayNetworking.send(
        viewer,
        new StageNamePayload(
            bearer.getId(),
            shownStage,
            RankLadder.of(bearer.getRegistryManager()).markOf(OathAttachment.get(bearer)),
            Titles.shown(
                ChallengeAttachment.get(bearer).feats(),
                Vampires.isVampire(bearer),
                Vampires.isVampire(viewer),
                FactionPlayerList.shown()),
            shownStage.flatMap(any -> HouseNames.of(bearer.getServer(), bearer.getUuid()))));
  }
}
