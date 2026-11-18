package com.grahambartley.nightborn.sun;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;

public final class ParasolEvents {

  private ParasolEvents() {}

  public static void register() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
        .register(entries -> entries.addAfter(Items.SHEARS, Parasols.PARASOL));
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
