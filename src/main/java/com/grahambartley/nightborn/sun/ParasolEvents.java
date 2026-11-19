package com.grahambartley.nightborn.sun;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class ParasolEvents {

  private ParasolEvents() {}

  public static void register() {
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> ParasolWalks.forget(handler.getPlayer().getUuid()));
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (entity, source, baseDamageTaken, damageTaken, blocked) -> {
          if (ParasolRules.blow(source.getAttacker() != null, damageTaken, blocked)) {
            Parasols.closeAll(entity);
          }
        });
  }
}
