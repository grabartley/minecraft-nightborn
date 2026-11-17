package com.grahambartley.nightborn.boss;

import java.util.List;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public final class BossAmbience {

  static final long UNSET = -1L;

  private long nextAt = UNSET;

  public static boolean due(final long nextAt, final long now) {
    return nextAt != UNSET && now >= nextAt;
  }

  public void tick(
      final ServerWorld world,
      final Arena arena,
      final List<ServerPlayerEntity> inside,
      final Optional<PhaseAmbience> ambience,
      final boolean engaged,
      final long now) {
    if (!engaged || ambience.isEmpty() || inside.isEmpty()) {
      nextAt = UNSET;
      return;
    }
    final PhaseAmbience heard = ambience.get();
    final Random random = world.getRandom();
    if (nextAt == UNSET) {
      nextAt = heard.nextAt(now, random.nextInt(heard.spread()));
      return;
    }
    if (!due(nextAt, now)) {
      return;
    }
    nextAt = heard.nextAt(now, random.nextInt(heard.spread()));
    final PhaseAmbience.Cue cue = heard.pick(random.nextInt(heard.totalWeight()));
    cue.sound()
        .map(SoundEvent::of)
        .ifPresent(
            sound ->
                inside.forEach(
                    player ->
                        player.playSoundToPlayer(
                            sound, SoundCategory.AMBIENT, cue.volume(), cue.pitch())));
    if (cue.lightning()) {
      final double angle = random.nextDouble() * 2.0 * Math.PI;
      BossReactions.strike(
          world,
          new Vec3d(
              arena.midX() + Math.cos(angle) * arena.radius(),
              arena.centre().getY(),
              arena.midZ() + Math.sin(angle) * arena.radius()));
    }
  }
}
