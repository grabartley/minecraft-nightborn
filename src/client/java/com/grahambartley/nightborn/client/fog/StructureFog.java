package com.grahambartley.nightborn.client.fog;

import com.grahambartley.nightborn.client.ritual.RitualGloom;
import com.grahambartley.nightborn.client.structure.NearbyStructure;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;

public final class StructureFog {

  private static float shown;

  private StructureFog() {}

  public static void register() {
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> shown = 0.0F);
    ClientTickEvents.END_CLIENT_TICK.register(StructureFog::tick);
  }

  private static void tick(final MinecraftClient client) {
    final float gloom = RitualGloom.strength(client.player);
    if (NearbyStructure.zone().isEmpty() && gloom == 0.0F && shown == 0.0F) {
      return;
    }
    shown = FogDepth.approach(shown, Math.max(NearbyStructure.closeness(client.player), gloom));
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
