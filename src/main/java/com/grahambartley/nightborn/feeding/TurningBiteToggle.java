package com.grahambartley.nightborn.feeding;

import com.grahambartley.nightborn.network.ToggleTurningBitePayload;
import com.grahambartley.nightborn.vampire.Vampires;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class TurningBiteToggle {

  static final String ON = "nightborn.turning_bite.on";
  static final String OFF = "nightborn.turning_bite.off";

  private TurningBiteToggle() {}

  public static void register() {
    ServerPlayNetworking.registerGlobalReceiver(
        ToggleTurningBitePayload.ID, (payload, context) -> toggle(context.player()));
  }

  public static boolean toggle(final ServerPlayerEntity player) {
    if (!Vampires.isVampire(player)) {
      return false;
    }
    final boolean on = !TurningBite.isOn(player);
    TurningBite.set(player, on);
    player.sendMessage(Text.translatable(messageKey(on)), true);
    return true;
  }

  static String messageKey(final boolean on) {
    return on ? ON : OFF;
  }
}
