package com.grahambartley.nightborn.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;

public final class HouseholdSpawner {

  private static final float FULL_TURN = 360.0F;

  private HouseholdSpawner() {}

  public static List<Entity> settle(
      final StructureWorldAccess world,
      final BlockBox chunkBox,
      final BlockBox heart,
      final Household household,
      final Random random) {
    final BlockPos centre = heart.getCenter();
    if (centre.getX() < chunkBox.getMinX()
        || centre.getX() > chunkBox.getMaxX()
        || centre.getZ() < chunkBox.getMinZ()
        || centre.getZ() > chunkBox.getMaxZ()) {
      return List.of();
    }
    final List<BlockPos> spots = spots(world, chunkBox, heart);
    final List<Entity> settled = new ArrayList<>();
    household
        .head()
        .flatMap(head -> HouseholdSpots.middle(spots, centre).map(spot -> Map.entry(head, spot)))
        .ifPresent(
            seat -> {
              spots.remove(seat.getValue());
              spawn(
                      world,
                      seat.getKey().resident().value(),
                      seat.getValue(),
                      seat.getKey().stages(),
                      centre,
                      random)
                  .ifPresent(settled::add);
            });
    final int count = household.residents().get(random);
    for (final BlockPos spot : HouseholdSpots.choose(spots, count, random)) {
      spawn(world, household.resident().value(), spot, household.stages(), centre, random)
          .ifPresent(settled::add);
    }
    return settled;
  }

  private static Optional<Entity> spawn(
      final StructureWorldAccess world,
      final EntityType<?> type,
      final BlockPos spot,
      final Map<Identifier, Integer> stages,
      final BlockPos centre,
      final Random random) {
    final Entity resident = type.create(world.toServerWorld());
    if (resident == null) {
      return Optional.empty();
    }
    resident.refreshPositionAndAngles(
        spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, random.nextFloat() * FULL_TURN, 0);
    if (resident instanceof MobEntity mob) {
      mob.initialize(
          world,
          world.getLocalDifficulty(spot),
          SpawnReason.STRUCTURE,
          new HouseholdArrival(stages));
      mob.setPersistent();
    }
    if (resident instanceof HouseholdMember member) {
      member.settleAt(centre);
    }
    world.spawnEntityAndPassengers(resident);
    return Optional.of(resident);
  }

  private static List<BlockPos> spots(
      final StructureWorldAccess world, final BlockBox chunkBox, final BlockBox heart) {
    final List<BlockPos> spots = new ArrayList<>();
    final int minX = Math.max(heart.getMinX(), chunkBox.getMinX());
    final int maxX = Math.min(heart.getMaxX(), chunkBox.getMaxX());
    final int minZ = Math.max(heart.getMinZ(), chunkBox.getMinZ());
    final int maxZ = Math.min(heart.getMaxZ(), chunkBox.getMaxZ());
    final BlockPos.Mutable floor = new BlockPos.Mutable();
    for (int x = minX; x <= maxX; x++) {
      for (int z = minZ; z <= maxZ; z++) {
        for (int y = heart.getMinY(); y < heart.getMaxY() - 1; y++) {
          floor.set(x, y, z);
          if (StandingSpot.fits(
              world.getBlockState(floor),
              world.getBlockState(floor.up()),
              world.getBlockState(floor.up(2)))) {
            spots.add(floor.up().toImmutable());
          }
        }
      }
    }
    return spots;
  }
}
