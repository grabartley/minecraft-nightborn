package com.grahambartley.nightborn.client.structure;

import com.grahambartley.nightborn.sound.NightbornSounds;
import com.grahambartley.nightborn.structure.AmbientAdditions;
import com.grahambartley.nightborn.structure.StructureZone;
import java.util.Optional;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class StructureAmbiencePlayer {

  private static AmbienceLoop loop;
  private static Identifier loopSound;

  private StructureAmbiencePlayer() {}

  public static void register() {
    ClientTickEvents.END_CLIENT_TICK.register(StructureAmbiencePlayer::tick);
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> stop(client));
  }

  private static void tick(final MinecraftClient client) {
    final ClientPlayerEntity player = client.player;
    final Optional<StructureZone> zone = NearbyStructure.zone();
    if (player == null || client.isPaused() || zone.isEmpty() && loop == null) {
      return;
    }
    final float closeness = NearbyStructure.closeness(player);
    playLoop(client, zone.flatMap(z -> z.ambience().loop()), closeness);
    zone.flatMap(z -> z.ambience().additions())
        .ifPresent(additions -> maybeAdd(client, player, additions, closeness));
  }

  private static void playLoop(
      final MinecraftClient client, final Optional<Identifier> wanted, final float closeness) {
    final Optional<Identifier> playing = loop == null ? Optional.empty() : Optional.of(loopSound);
    final boolean done = loop != null && loop.isDone();
    switch (AmbienceMix.loopAction(playing, done, wanted, closeness)) {
      case KEEP -> loop.aimAt(closeness);
      case START -> start(client, wanted.get(), closeness);
      case REPLACE -> {
        loop.fadeOut();
        start(client, wanted.get(), closeness);
      }
      case STOP -> {
        loop.fadeOut();
        loop = null;
      }
      case NOTHING -> loop = null;
    }
  }

  private static void start(
      final MinecraftClient client, final Identifier sound, final float closeness) {
    loopSound = sound;
    loop = new AmbienceLoop(SoundEvent.of(sound), closeness);
    client.getSoundManager().play(loop);
  }

  private static void maybeAdd(
      final MinecraftClient client,
      final ClientPlayerEntity player,
      final AmbientAdditions additions,
      final float closeness) {
    if (AmbienceMix.addsOneShot(
        additions.tickChance(), closeness, player.getRandom().nextDouble())) {
      client
          .getSoundManager()
          .play(
              PositionedSoundInstance.ambient(
                  SoundEvent.of(additions.sound()),
                  NightbornSounds.variedPitch(player.getRandom()),
                  AmbienceMix.additionVolume(closeness)));
    }
  }

  private static void stop(final MinecraftClient client) {
    if (loop != null) {
      client.getSoundManager().stop(loop);
      loop = null;
    }
  }
}
