package com.grahambartley.nightborn.client;

import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.network.SyncConfigPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class SyncedServerConfig {

  private static volatile NightbornConfig current = NightbornConfig.DEFAULTS;

  private SyncedServerConfig() {}

  public static void register() {
    ClientPlayNetworking.registerGlobalReceiver(
        SyncConfigPayload.ID, (payload, context) -> current = payload.config());
    ClientPlayConnectionEvents.DISCONNECT.register(
        (handler, client) -> current = NightbornConfig.DEFAULTS);
  }

  public static NightbornConfig get() {
    return current;
  }
}
