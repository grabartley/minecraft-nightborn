package com.grahambartley.nightborn.structure;

import java.util.Optional;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.structure.Structure;

public final class EntrancePlan {

  private EntrancePlan() {}

  public static Optional<BlockPos> of(
      final ServerWorld world, final Structure structure, final ChunkPos start) {
    final ChunkGenerator generator = world.getChunkManager().getChunkGenerator();
    final Structure.Context context =
        new Structure.Context(
            world.getRegistryManager(),
            generator,
            generator.getBiomeSource(),
            world.getChunkManager().getNoiseConfig(),
            world.getStructureTemplateManager(),
            world.getSeed(),
            start,
            world,
            structure.getValidBiomes()::contains);
    return structure
        .getValidStructurePosition(context)
        .map(position -> entrance(structure, position, context).orElse(position.position()));
  }

  public static BlockPos ofLocated(
      final ServerWorld world, final RegistryEntry<Structure> structure, final BlockPos locate) {
    final StructurePlacementCalculator placements =
        world.getChunkManager().getStructurePlacementCalculator();
    final ChunkPos near = new ChunkPos(locate);
    for (final StructurePlacement placement : placements.getPlacements(structure)) {
      if (placement instanceof RandomSpreadStructurePlacement spread) {
        final ChunkPos start = spread.getStartChunk(placements.getStructureSeed(), near.x, near.z);
        if (spread.getLocatePos(start).equals(locate)) {
          return of(world, structure.value(), start).orElse(locate);
        }
      }
    }
    return locate;
  }

  private static Optional<BlockPos> entrance(
      final Structure structure,
      final Structure.StructurePosition position,
      final Structure.Context context) {
    if (!(structure instanceof VampireStructure vampire)) {
      return Optional.empty();
    }
    return vampire
        .siting()
        .entrance()
        .flatMap(
            name ->
                Threshold.in(
                    position.generate().toList().pieces(),
                    name,
                    context.structureTemplateManager(),
                    context.random()))
        .map(StructureEntrance::standingSpot);
  }
}
