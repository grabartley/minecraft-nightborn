package com.grahambartley.nightborn.client.boss;

import com.grahambartley.nightborn.boss.Arena;
import com.grahambartley.nightborn.network.BossArenaPayload;
import java.util.Optional;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;

public final class BossArenaView {

  private static Optional<Arena> arena = Optional.empty();
  private static Optional<MusicSound> music = Optional.empty();

  private BossArenaView() {}

  public static void register() {
    ClientPlayNetworking.registerGlobalReceiver(
        BossArenaPayload.ID, (payload, context) -> show(payload));
    ClientPlayConnectionEvents.DISCONNECT.register(
        (handler, client) -> show(BossArenaPayload.left()));
  }

  private static void show(final BossArenaPayload payload) {
    arena = payload.arena();
    music = music(payload);
  }

  static Optional<MusicSound> music(final BossArenaPayload payload) {
    return payload
        .music()
        .filter(id -> payload.arena().isPresent() && payload.fighting())
        .map(id -> new MusicSound(RegistryEntry.of(SoundEvent.of(id)), 0, 0, true));
  }

  public static Optional<Arena> arena() {
    return arena;
  }

  public static Optional<MusicSound> music() {
    return music;
  }
}
