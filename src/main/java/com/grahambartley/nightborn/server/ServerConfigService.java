package com.grahambartley.nightborn.server;

import com.grahambartley.nightborn.config.ConfigFile;
import com.grahambartley.nightborn.config.ConfigPaths;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.network.EditConfigPayload;
import com.grahambartley.nightborn.network.SyncConfigPayload;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;

public final class ServerConfigService {

  private static final List<BiConsumer<MinecraftServer, NightbornConfig>> LISTENERS =
      new ArrayList<>();

  private static volatile NightbornConfig current = NightbornConfig.DEFAULTS;

  private ServerConfigService() {}

  public static void register() {
    ServerLifecycleEvents.SERVER_STARTING.register(ServerConfigService::load);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> current = NightbornConfig.DEFAULTS);
    ServerPlayConnectionEvents.JOIN.register(
        (handler, sender, server) -> sendTo(handler.getPlayer()));
    ServerPlayNetworking.registerGlobalReceiver(
        EditConfigPayload.ID,
        (payload, context) -> {
          final ServerPlayerEntity player = context.player();
          if (ConfigEditPermission.canEdit(player, player.getServer())) {
            update(player.getServer(), payload.config());
          } else {
            sendTo(player);
          }
        });
  }

  public static NightbornConfig current() {
    return current;
  }

  public static void onChange(final BiConsumer<MinecraftServer, NightbornConfig> listener) {
    LISTENERS.add(listener);
  }

  public static boolean update(final MinecraftServer server, final NightbornConfig updated) {
    if (!fileFor(server).save(updated)) {
      return false;
    }
    current = updated;
    final SyncConfigPayload payload = new SyncConfigPayload(updated);
    for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      ServerPlayNetworking.send(player, payload);
    }
    LISTENERS.forEach(listener -> listener.accept(server, updated));
    return true;
  }

  private static void load(final MinecraftServer server) {
    current = fileFor(server).loadOrCreate();
  }

  private static void sendTo(final ServerPlayerEntity player) {
    ServerPlayNetworking.send(player, new SyncConfigPayload(current));
  }

  private static ConfigFile fileFor(final MinecraftServer server) {
    return new ConfigFile(
        ConfigPaths.serverConfig(server.getSavePath(WorldSavePath.ROOT).normalize()),
        Clock.systemUTC());
  }
}
