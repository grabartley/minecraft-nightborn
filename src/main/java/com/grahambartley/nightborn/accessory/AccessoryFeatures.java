package com.grahambartley.nightborn.accessory;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class AccessoryFeatures {

  private AccessoryFeatures() {}

  public static void register() {
    Accessories.register();
    GiftAttributes.register();
    AccessoryItems.register();
    AccessoryLoot.register();
    ServerPlayConnectionEvents.JOIN.register(
        (handler, sender, server) -> AccessoryMove.fromArmour(handler.getPlayer()));
    ServerLivingEntityEvents.AFTER_DAMAGE.register(StrikeHealing::afterDamage);
    ServerTickEvents.END_SERVER_TICK.register(Vigil::tick);
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> {
          StrikeHealing.forget(handler.getPlayer().getUuid());
          Vigil.forget(handler.getPlayer().getUuid());
        });
    ServerLifecycleEvents.SERVER_STOPPED.register(
        server -> {
          StrikeHealing.clearAll();
          Vigil.clearAll();
        });
  }
}
