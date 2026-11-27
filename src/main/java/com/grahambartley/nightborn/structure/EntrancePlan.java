package com.grahambartley.nightborn.structure;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.structure.Structure;

public final class EntrancePlan {

  private EntrancePlan() {}

  public static Structure.Context context(
      final ServerWorld world, final ChunkPos start, final Predicate<RegistryEntry<Biome>> biomes) {
    final ChunkGenerator generator = world.getChunkManager().getChunkGenerator();
    return new Structure.Context(
        world.getRegistryManager(),
        generator,
        generator.getBiomeSource(),
        world.getChunkManager().getNoiseConfig(),
        world.getStructureTemplateManager(),
        world.getSeed(),
        start,
        world,
        biomes);
  }

  public static Optional<Threshold> threshold(
      final Structure structure,
      final List<StructurePiece> pieces,
      final StructureTemplateManager templates,
      final Random random) {
    return structure instanceof VampireStructure vampire
        ? vampire.siting().entrance().flatMap(name -> Threshold.in(pieces, name, templates, random))
        : Optional.empty();
  }

  public static Optional<BlockPos> of(
      final ServerWorld world, final Structure structure, final ChunkPos start) {
    final Structure.Context context = context(world, start, structure.getValidBiomes()::contains);
    return structure
        .getValidStructurePosition(context)
        .map(
            position ->
                standingSpot(
                        structure,
                        position.generate().toList().pieces(),
                        context.structureTemplateManager(),
                        context.random())
                    .orElse(position.position()));
  }

  public static Optional<BlockPos> ofSaved(
      final ServerWorld world, final Structure structure, final NbtCompound saved) {
    final StructureStart start =
        StructureStart.fromNbt(StructureContext.from(world), saved, world.getSeed());
    return start == null || !start.hasChildren()
        ? Optional.empty()
        : standingSpot(
            structure,
            start.getChildren(),
            world.getStructureTemplateManager(),
            Random.create(world.getSeed()));
  }

  public static BlockPos ofLocated(
      final ServerWorld world, final RegistryEntry<Structure> structure, final BlockPos locate) {
    final StructurePlacementCalculator placements =
        world.getChunkManager().getStructurePlacementCalculator();
    for (final StructurePlacement placement : placements.getPlacements(structure)) {
      if (placement instanceof RandomSpreadStructurePlacement spread) {
        final Optional<ChunkPos> start =
            SpreadStart.behind(spread, placements.getStructureSeed(), locate);
        if (start.isPresent()) {
          return of(world, structure.value(), start.get()).orElse(locate);
        }
      }
    }
    return locate;
  }

  private static Optional<BlockPos> standingSpot(
      final Structure structure,
      final List<StructurePiece> pieces,
      final StructureTemplateManager templates,
      final Random random) {
    return threshold(structure, pieces, templates, random)
        .map(way -> ((VampireStructure) structure).siting().standingSpot(way));
  }
}
