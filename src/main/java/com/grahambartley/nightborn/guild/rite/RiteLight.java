package com.grahambartley.nightborn.guild.rite;

import com.grahambartley.nightborn.boss.ZoneMarkEntity;
import java.util.List;
import java.util.Optional;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;

final class RiteLight {

  static final double MOTE_SPREAD = 0.35;
  static final double MOTE_HEIGHT = 1.0;
  static final double MOTE_DRIFT = 0.02;

  private RiteLight() {}

  static Optional<ZoneMarkEntity> shine(
      final ServerWorld world, final Vec3d at, final GuildRite.Light light, final int ticks) {
    return ZoneMarkEntity.ground(
        world, at, light.radius(), Optional.of(light.look()), ticks, light.rise());
  }

  static void motes(final ServerWorld world, final Vec3d at, final GuildRite.Light light) {
    world.spawnParticles(
        ParticleTypes.END_ROD,
        at.x,
        at.y + MOTE_HEIGHT,
        at.z,
        light.motes(),
        MOTE_SPREAD,
        MOTE_HEIGHT / 2,
        MOTE_SPREAD,
        MOTE_DRIFT);
  }

  static void sounds(
      final ServerWorld world, final Vec3d at, final List<GuildRite.Cue> cues, final int ticks) {
    GuildRite.Cue.due(cues, ticks)
        .forEach(
            cue ->
                Registries.SOUND_EVENT
                    .getOrEmpty(cue.sound())
                    .ifPresent(
                        event ->
                            world.playSound(
                                null,
                                at.x,
                                at.y,
                                at.z,
                                event,
                                SoundCategory.NEUTRAL,
                                cue.volume(),
                                cue.pitch())));
  }
}
