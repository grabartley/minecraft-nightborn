package com.grahambartley.nightborn.wild;

import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.core.SurfaceSpots;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.home.NightRoutine;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;

public final class WildVampires {

  static final int RING = 32;

  private WildVampires() {}

  static NightLog night(
      final ServerWorld world,
      final ServerPlayerEntity player,
      final NightLog log,
      final NightbornConfig config) {
    final long time = world.getTimeOfDay();
    final long tonight = NightRoutine.night(time);
    if (!NightTime.isNight(world.getDimension().hasFixedTime(), time)
        || log.rolled(tonight)
        || !spawns(world)) {
      return log;
    }
    final Random random = world.getRandom();
    NightLog rolled = log.rolledOn(tonight);
    if (WildRules.rolls(
        random.nextInt(WildRules.PERCENT), config.structures().wildVampireChance())) {
      wild(world, player, config.structures().wildVampireDistance(), random);
    }
    if (!rolled.watched()
        && rolled.isNew(tonight, config.firstNights().firstNights())
        && WildRules.rolls(random.nextInt(WildRules.PERCENT), config.firstNights().watcherChance())
        && watcher(world, player, config.firstNights().watcherDistance(), random)) {
      rolled = rolled.watchedOnce();
    }
    return rolled;
  }

  static boolean spawns(final ServerWorld world) {
    return world.getDifficulty() != Difficulty.PEACEFUL
        && world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING);
  }

  public static Optional<VampireEntity> wild(
      final ServerWorld world,
      final ServerPlayerEntity player,
      final int distance,
      final Random random) {
    final List<BlockPos> players =
        world.getPlayers(other -> !other.isSpectator()).stream().map(Entity::getBlockPos).toList();
    return SurfaceSpots.around(world, player.getBlockPos(), distance, distance + RING, random)
        .filter(spot -> WildRules.farFromEvery(spot, players, distance))
        .flatMap(spot -> place(world, spot, Wanderer.wild(world.getTime()), random));
  }

  public static boolean watcher(
      final ServerWorld world,
      final ServerPlayerEntity player,
      final int distance,
      final Random random) {
    return SurfaceSpots.around(world, player.getBlockPos(), distance, distance + RING / 4, random)
        .flatMap(
            spot ->
                place(world, spot, Wanderer.watching(player.getUuid(), world.getTime()), random))
        .isPresent();
  }

  private static Optional<VampireEntity> place(
      final ServerWorld world, final BlockPos spot, final Wanderer wanderer, final Random random) {
    final VampireEntity vampire = NightbornMobs.VAMPIRE.create(world);
    if (vampire == null) {
      return Optional.empty();
    }
    vampire.refreshPositionAndAngles(spot, random.nextFloat() * 360.0F, 0.0F);
    vampire.initialize(world, world.getLocalDifficulty(spot), SpawnReason.EVENT, null);
    vampire.setPersistent();
    WandererAttachment.set(vampire, wanderer);
    world.spawnEntityAndPassengers(vampire);
    return Optional.of(vampire);
  }
}
