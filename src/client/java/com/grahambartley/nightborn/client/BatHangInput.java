package com.grahambartley.nightborn.client;

import com.grahambartley.nightborn.batform.BatHang;
import com.grahambartley.nightborn.batform.BatHangRules;
import com.grahambartley.nightborn.network.ReleaseBatHangPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

final class BatHangInput {

  private BatHangInput() {}

  static void register() {
    ClientTickEvents.END_CLIENT_TICK.register(BatHangInput::tick);
  }

  private static void tick(final MinecraftClient client) {
    final ClientPlayerEntity player = client.player;
    if (player != null
        && client.currentScreen == null
        && BatHangRules.letsGo(
            BatHang.isHanging(player), player.input.jumping, player.input.hasForwardMovement())
        && ClientPlayNetworking.canSend(ReleaseBatHangPayload.ID)) {
      ClientPlayNetworking.send(ReleaseBatHangPayload.INSTANCE);
    }
  }
}
