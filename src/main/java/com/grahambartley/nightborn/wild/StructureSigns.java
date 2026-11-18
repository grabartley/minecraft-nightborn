package com.grahambartley.nightborn.wild;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.core.SurfaceSpots;
import com.grahambartley.nightborn.guild.GuildRumour;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireBatEntity;
import com.grahambartley.nightborn.servant.job.ScoutChoices;
import com.grahambartley.nightborn.structure.NearestStructures;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

public final class StructureSigns {

  public static final TagKey<EntityType<?>> DRAINED_ANIMALS =
      TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Nightborn.MOD_ID, "drained_animals"));

  static final int NEAR = 16;
  static final int FAR = 32;
  static final float BEHIND_SPREAD = MathHelper.HALF_PI;
  static final int ROOST_TRIES = 24;
  static final int ROOST_SEARCH = 24;

  private StructureSigns() {}

  static void show(
      final ServerWorld world, final ServerPlayerEntity player, final int chance, final int range) {
    final Random random = world.getRandom();
    if (!WildRules.rolls(random.nextInt(WildRules.PERCENT), chance)) {
      return;
    }
    final BlockPos from = player.getBlockPos();
    final boolean bat = random.nextBoolean();
    NearestStructures.eachOf(world, GuildRumour.VAMPIRE_HOUSES, from, range)
        .thenAcceptAsync(
            found -> {
              if (ScoutChoices.nearest(from, found, range, 1).isEmpty()
                  || player.isDisconnected()) {
                return;
              }
              if (!bat || !roostBat(world, player, random)) {
                drainedAnimal(world, player, random);
              }
            },
            world.getServer());
  }

  public static boolean drainedAnimal(
      final ServerWorld world, final ServerPlayerEntity player, final Random random) {
    final List<RegistryEntry<EntityType<?>>> kinds =
        Registries.ENTITY_TYPE
            .getEntryList(DRAINED_ANIMALS)
            .map(list -> list.stream().toList())
            .orElse(List.of());
    if (kinds.isEmpty()) {
      return false;
    }
    final float behind = behind(player.getYaw(), random.nextFloat());
    final Optional<BlockPos> spot =
        SurfaceSpots.at(
            world,
            SurfaceSpots.offset(player.getBlockPos(), behind, random.nextBetween(NEAR, FAR)));
    if (spot.isEmpty()) {
      return false;
    }
    final Entity animal = kinds.get(random.nextInt(kinds.size())).value().create(world);
    if (!(animal instanceof LivingEntity body)) {
      return false;
    }
    body.refreshPositionAndAngles(spot.get(), random.nextFloat() * 360.0F, 0.0F);
    world.spawnEntity(body);
    body.setSilent(true);
    body.damage(world.getDamageSources().create(NightbornDamage.BITE), Float.MAX_VALUE);
    return true;
  }

  static float behind(final float yawDegrees, final float roll) {
    return yawDegrees * MathHelper.RADIANS_PER_DEGREE
        - MathHelper.HALF_PI
        + (roll - 0.5F) * BEHIND_SPREAD;
  }

  public static boolean roostBat(
      final ServerWorld world, final ServerPlayerEntity player, final Random random) {
    for (int i = 0; i < ROOST_TRIES; i++) {
      final Optional<BlockPos> under =
          roost(
              world,
              SurfaceSpots.offset(
                  player.getBlockPos(),
                  random.nextFloat() * MathHelper.TAU,
                  random.nextBetween(NEAR / 2, FAR)));
      if (under.isPresent()) {
        final VampireBatEntity bat = NightbornMobs.VAMPIRE_BAT.create(world);
        if (bat == null) {
          return false;
        }
        bat.refreshPositionAndAngles(under.get(), random.nextFloat() * 360.0F, 0.0F);
        bat.initialize(world, world.getLocalDifficulty(under.get()), SpawnReason.EVENT, null);
        bat.setRoosting(true);
        world.spawnEntity(bat);
        return true;
      }
    }
    return false;
  }

  static Optional<BlockPos> roost(final ServerWorld world, final BlockPos column) {
    if (!world.isChunkLoaded(column.getX() >> 4, column.getZ() >> 4)) {
      return Optional.empty();
    }
    final int top = world.getTopY(Heightmap.Type.MOTION_BLOCKING, column.getX(), column.getZ());
    for (int y = top - 1; y > top - ROOST_SEARCH; y--) {
      final BlockPos branch = new BlockPos(column.getX(), y, column.getZ());
      final BlockPos below = branch.down();
      if (world.getBlockState(branch).isSolidBlock(world, branch)
          && world.isAir(below)
          && world.isAir(below.down())) {
        return Optional.of(below);
      }
    }
    return Optional.empty();
  }
}
