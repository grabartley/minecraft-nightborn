package com.grahambartley.nightborn.client.fog;

import com.grahambartley.nightborn.network.StructureFogPayload;
import com.grahambartley.nightborn.structure.FogZone;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Optional;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public final class StructureFog {

  private static Optional<FogZone> zone = Optional.empty();
  private static float shown;

  private StructureFog() {}

  public static void register() {
    ClientPlayNetworking.registerGlobalReceiver(
        StructureFogPayload.ID, (payload, context) -> zone = payload.zone());
    ClientPlayConnectionEvents.DISCONNECT.register(
        (handler, client) -> {
          zone = Optional.empty();
          shown = 0.0F;
        });
    ClientTickEvents.END_CLIENT_TICK.register(StructureFog::tick);
  }

  private static void tick(final MinecraftClient client) {
    if (zone.isEmpty() && shown == 0.0F) {
      return;
    }
    final ClientPlayerEntity player = client.player;
    final float target =
        player == null
            ? 0.0F
            : zone.map(z -> z.closeness(player.getX(), player.getEyeY(), player.getZ()))
                .orElse(0.0F);
    shown = FogDepth.approach(shown, target);
  }

  public static void apply() {
    if (shown <= 0.0F) {
      return;
    }
    final float end = FogDepth.end(RenderSystem.getShaderFogEnd(), shown);
    RenderSystem.setShaderFogStart(FogDepth.start(RenderSystem.getShaderFogStart(), shown, end));
    RenderSystem.setShaderFogEnd(end);
  }
}
