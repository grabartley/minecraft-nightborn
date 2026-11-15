package com.grahambartley.nightborn.server;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public final class ConfigEditPermission {

  public static final int OPERATOR_LEVEL = 2;

  private ConfigEditPermission() {}

  public static boolean canEdit(final PlayerEntity player, @Nullable final MinecraftServer server) {
    return player.hasPermissionLevel(OPERATOR_LEVEL)
        || (server != null && server.isHost(player.getGameProfile()));
  }
}
