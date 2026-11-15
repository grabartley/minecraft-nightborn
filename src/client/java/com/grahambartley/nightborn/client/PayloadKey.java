package com.grahambartley.nightborn.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.CustomPayload;

final class PayloadKey {

  private PayloadKey() {}

  static void register(final String name, final int defaultKey, final CustomPayload payload) {
    final KeyBinding key =
        KeyBindingHelper.registerKeyBinding(
            new KeyBinding(name, InputUtil.Type.KEYSYM, defaultKey, SkillKeys.CATEGORY));
    ClientTickEvents.END_CLIENT_TICK.register(client -> send(client, key, payload));
  }

  private static void send(
      final MinecraftClient client, final KeyBinding key, final CustomPayload payload) {
    while (key.wasPressed()) {
      if (client.currentScreen == null && ClientPlayNetworking.canSend(payload.getId())) {
        ClientPlayNetworking.send(payload);
      }
    }
  }
}
