package com.grahambartley.nightborn.inquisitor;

import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.hunt.Hunts;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.SkillEarning;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class InquisitorDues {

  private InquisitorDues() {}

  static void register() {
    ServerPlayConnectionEvents.JOIN.register(
        (handler, sender, server) ->
            InquisitorLedger.of(server)
                .settle(handler.getPlayer().getUuid())
                .forEach(owed -> apply(handler.getPlayer(), owed)));
  }

  public static void owe(
      final MinecraftServer server, final UUID player, final List<InquisitorLedger.Owed> owed) {
    final ServerPlayerEntity online = server.getPlayerManager().getPlayer(player);
    if (online == null) {
      InquisitorLedger.of(server).owe(player, owed);
      return;
    }
    owed.forEach(entry -> apply(online, entry));
  }

  static void apply(final ServerPlayerEntity player, final InquisitorLedger.Owed owed) {
    switch (owed) {
      case END_VISIT -> Hunts.endVisit(player);
      case COME_BACK -> Hunts.returnLater(player);
      case CLEAR_NOTORIETY -> Hunts.set(player, 0.0);
      case PAY ->
          SkillEarning.fromInquisitor(
              player,
              ServerConfigService.current().inquisitor().experience(),
              ServerConfigService.current().skills());
      case ENDED -> Deeds.record(player, Deed.of(DeedKinds.INQUISITOR_ENDED));
    }
  }
}
