package com.grahambartley.nightborn.client.boss;

import com.grahambartley.nightborn.boss.Arena;
import com.grahambartley.nightborn.boss.ArenaWall;
import com.grahambartley.nightborn.network.BossArenaPayload;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class BossArenaView {

  static final int NONE = -1;

  private static int boss = NONE;
  private static Optional<Arena> arena = Optional.empty();
  private static Optional<MusicSound> music = Optional.empty();
  private static boolean fighting;
  private static Optional<ArenaWall> wall = Optional.empty();
  private static final Set<Identifier> BOSS_TRACKS = new HashSet<>();

  private BossArenaView() {}

  public static void register() {
    ClientPlayNetworking.registerGlobalReceiver(
        BossArenaPayload.ID, (payload, context) -> show(payload));
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> forget());
  }

  static void show(final BossArenaPayload payload) {
    if (!applies(boss, payload.boss(), payload.arena().isPresent())) {
      return;
    }
    payload.music().ifPresent(BOSS_TRACKS::add);
    boss = payload.arena().isPresent() ? payload.boss() : NONE;
    arena = payload.arena();
    music = music(payload);
    fighting = payload.arena().isPresent() && payload.fighting();
    wall = payload.arena().isPresent() ? payload.wall() : Optional.empty();
  }

  static void forget() {
    boss = NONE;
    arena = Optional.empty();
    music = Optional.empty();
    fighting = false;
    wall = Optional.empty();
    BOSS_TRACKS.clear();
  }

  static boolean applies(final int shown, final int sender, final boolean entering) {
    return entering || shown == NONE || shown == sender;
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

  public static Optional<ArenaWall> wall() {
    return wall;
  }

  public static boolean fighting() {
    return fighting;
  }

  public static Optional<MusicSound> music() {
    return music;
  }

  public static Optional<Identifier> track() {
    return track(music);
  }

  static Optional<Identifier> track(final Optional<MusicSound> held) {
    return held.map(sound -> sound.getSound().value().getId());
  }

  public static Set<Identifier> bossTracks() {
    return Collections.unmodifiableSet(BOSS_TRACKS);
  }
}
