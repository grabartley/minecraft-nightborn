package com.grahambartley.nightborn.client;

import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.network.UsePowerPayload;
import com.grahambartley.nightborn.screen.SkillScreen;
import com.grahambartley.nightborn.skill.PowerBar;
import com.grahambartley.nightborn.skill.PowerHeading;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class SkillKeys {

  static final String CATEGORY = "key.category.nightborn";
  static final int[] POWER_DEFAULTS = {
    GLFW.GLFW_KEY_R, GLFW.GLFW_KEY_G, GLFW.GLFW_KEY_V, GLFW.GLFW_KEY_B, GLFW.GLFW_KEY_N
  };

  static final String NO_SIDE = "nightborn.skills.no_side";
  private static KeyBinding openSkills;
  private static final List<KeyBinding> POWERS = new ArrayList<>();

  private SkillKeys() {}

  public static void register() {
    openSkills =
        KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.nightborn.skills", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY));
    for (int i = 0; i < PowerBar.SLOTS; i++) {
      POWERS.add(
          KeyBindingHelper.registerKeyBinding(
              new KeyBinding(
                  "key.nightborn.power_" + (i + 1),
                  InputUtil.Type.KEYSYM,
                  POWER_DEFAULTS[i],
                  CATEGORY)));
    }
    ClientTickEvents.END_CLIENT_TICK.register(SkillKeys::tick);
  }

  public static KeyBinding openSkills() {
    return openSkills;
  }

  public static KeyBinding power(final int slot) {
    return POWERS.get(slot);
  }

  private static void tick(final MinecraftClient client) {
    while (openSkills.wasPressed()) {
      if (client.currentScreen != null || client.player == null) {
        continue;
      }
      if (GuildMembership.sided(client.player)) {
        client.setScreen(new SkillScreen());
      } else {
        client.player.sendMessage(Text.translatable(NO_SIDE), true);
      }
    }
    for (int i = 0; i < POWERS.size(); i++) {
      while (POWERS.get(i).wasPressed()) {
        if (client.currentScreen == null
            && client.player != null
            && ClientPlayNetworking.canSend(UsePowerPayload.ID)) {
          final Input input = client.player.input;
          ClientPlayNetworking.send(
              new UsePowerPayload(
                  i,
                  PowerHeading.of(
                      input.pressingForward,
                      input.pressingBack,
                      input.pressingLeft,
                      input.pressingRight)));
        }
      }
    }
  }
}
