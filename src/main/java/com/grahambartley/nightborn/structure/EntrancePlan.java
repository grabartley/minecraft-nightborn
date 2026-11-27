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
      final VampireStructure house,
      final List<StructurePiece> pieces,
      final StructureTemplateManager templates,
      final Random random) {
    return house.siting().entrance().flatMap(name -> Threshold.in(pieces, name, templates, random));
  }

  public static boolean plans(
      final ServerWorld world, final Structure structure, final ChunkPos start) {
    return structure
        .getValidStructurePosition(context(world, start, structure.getValidBiomes()::contains))
        .isPresent();
  }

  public static Optional<BlockPos> of(
      final ServerWorld world, final VampireStructure house, final ChunkPos start) {
    return house
        .getValidStructurePosition(context(world, start, house.getValidBiomes()::contains))
        .map(
            position ->
                standingSpot(world, house, position.generate().toList().pieces())
                    .orElse(position.position()));
  }

  public static Optional<BlockPos> ofSaved(
      final ServerWorld world, final VampireStructure house, final NbtCompound saved) {
    final StructureStart start =
        StructureStart.fromNbt(StructureContext.from(world), saved, world.getSeed());
    return start == null || !start.hasChildren()
        ? Optional.empty()
        : standingSpot(world, house, start.getChildren());
  }

  private static Optional<BlockPos> standingSpot(
      final ServerWorld world, final VampireStructure house, final List<StructurePiece> pieces) {
    return threshold(
            house, pieces, world.getStructureTemplateManager(), Random.create(world.getSeed()))
        .map(house.siting()::standingSpot);
  }
}
