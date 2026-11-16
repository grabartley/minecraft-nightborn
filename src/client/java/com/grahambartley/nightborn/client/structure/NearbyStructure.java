package com.grahambartley.nightborn.client.structure;

import com.grahambartley.nightborn.network.StructureZonePayload;
import com.grahambartley.nightborn.structure.StructureMusic;
import com.grahambartley.nightborn.structure.StructureZone;
import java.util.Optional;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.MusicSound;

public final class NearbyStructure {

  private static Optional<StructureZone> zone = Optional.empty();
  private static Optional<MusicSound> music = Optional.empty();

  private NearbyStructure() {}

  public static void register() {
    ClientPlayNetworking.registerGlobalReceiver(
        StructureZonePayload.ID, (payload, context) -> enter(payload.zone()));
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> enter(Optional.empty()));
  }

  private static void enter(final Optional<StructureZone> next) {
    zone = next;
    music = next.flatMap(z -> z.ambience().music()).map(StructureMusic::toMusic);
  }

  public static Optional<StructureZone> zone() {
    return zone;
  }

  public static Optional<MusicSound> music() {
    return music;
  }

  public static float closeness(final ClientPlayerEntity player) {
    return player == null
        ? 0.0F
        : zone.map(z -> z.area().closeness(player.getX(), player.getEyeY(), player.getZ()))
            .orElse(0.0F);
  }
}
