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

  static final double MOTE_RING = 0.8;
  static final double MOTE_LIFT = 0.2;
  static final double MOTE_HEIGHT = 0.5;
  static final double MOTE_DRIFT = 0.02;

  private RiteLight() {}

  static Optional<ZoneMarkEntity> shine(
      final ServerWorld world, final Vec3d at, final GuildRite.Light light, final int ticks) {
    return ZoneMarkEntity.ground(
        world, at, light.radius(), Optional.of(light.look()), ticks, light.rise());
  }

  static void motes(final ServerWorld world, final Vec3d at, final GuildRite.Light light) {
    for (int mote = 0; mote < light.motes(); mote++) {
      final double angle = world.getRandom().nextDouble() * Math.PI * 2;
      world.spawnParticles(
          ParticleTypes.END_ROD,
          at.x + Math.cos(angle) * light.radius() * MOTE_RING,
          at.y + MOTE_LIFT,
          at.z + Math.sin(angle) * light.radius() * MOTE_RING,
          1,
          0.0,
          MOTE_HEIGHT,
          0.0,
          MOTE_DRIFT);
    }
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
                                SoundCategory.PLAYERS,
                                cue.volume(),
                                cue.pitch())));
  }
}
