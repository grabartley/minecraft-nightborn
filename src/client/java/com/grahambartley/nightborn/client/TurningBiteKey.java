package com.grahambartley.nightborn.client;

import com.grahambartley.nightborn.network.ToggleTurningBitePayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class TurningBiteKey {

  private static KeyBinding turningBite;

  private TurningBiteKey() {}

  public static void register() {
    turningBite =
        KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.nightborn.turning_bite",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                SkillKeys.CATEGORY));
    ClientTickEvents.END_CLIENT_TICK.register(TurningBiteKey::tick);
  }

  private static void tick(final MinecraftClient client) {
    while (turningBite.wasPressed()) {
      if (client.currentScreen == null
          && ClientPlayNetworking.canSend(ToggleTurningBitePayload.ID)) {
        ClientPlayNetworking.send(ToggleTurningBitePayload.INSTANCE);
      }
    }
  }
}
