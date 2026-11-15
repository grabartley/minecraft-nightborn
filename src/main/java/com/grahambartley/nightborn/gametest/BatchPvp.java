package com.grahambartley.nightborn.gametest;

import net.minecraft.server.MinecraftServer;

final class BatchPvp {

  private static boolean before;

  private BatchPvp() {}

  static void set(final MinecraftServer server, final boolean enabled) {
    before = server.isPvpEnabled();
    server.setPvpEnabled(enabled);
  }

  static void restore(final MinecraftServer server) {
    server.setPvpEnabled(before);
  }
}
