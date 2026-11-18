package com.grahambartley.nightborn.accessory;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class AccessoryFeatures {

  private AccessoryFeatures() {}

  public static void register() {
    Accessories.register();
    AccessoryItems.register();
    ServerPlayConnectionEvents.JOIN.register(
        (handler, sender, server) -> AccessoryMove.fromArmour(handler.getPlayer()));
  }
}
