package com.grahambartley.nightborn.client;

import com.grahambartley.nightborn.network.ToggleLevitationPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class LevitationKey {

  private static KeyBinding levitate;

  private LevitationKey() {}

  public static void register() {
    levitate =
        KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.nightborn.levitate",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                SkillKeys.CATEGORY));
    ClientTickEvents.END_CLIENT_TICK.register(LevitationKey::tick);
  }

  private static void tick(final MinecraftClient client) {
    while (levitate.wasPressed()) {
      if (client.currentScreen == null
          && ClientPlayNetworking.canSend(ToggleLevitationPayload.ID)) {
        ClientPlayNetworking.send(ToggleLevitationPayload.INSTANCE);
      }
    }
  }
}
