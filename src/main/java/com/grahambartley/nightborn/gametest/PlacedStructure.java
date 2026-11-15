package com.grahambartley.nightborn.gametest;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.structure.StructurePiecesList;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;

final class PlacedStructure {

  private final WorldChunk home;
  private final Map<Structure, StructureStart> startsBefore;
  private final Map<WorldChunk, Map<Structure, LongSet>> referencesBefore = new HashMap<>();
  private final StructureStart start;

  private PlacedStructure(final ServerWorld world, final Structure structure, final BlockBox box) {
    home = world.getWorldChunk(box.getCenter());
    final ChunkPos homePos = home.getPos();
    start =
        new StructureStart(
            structure, homePos, 0, new StructurePiecesList(List.of(new Marker(box))));
    startsBefore = new HashMap<>(home.getStructureStarts());
    home.setStructureStart(structure, start);
    for (int x = box.getMinX() >> 4; x <= box.getMaxX() >> 4; x++) {
      for (int z = box.getMinZ() >> 4; z <= box.getMaxZ() >> 4; z++) {
        final WorldChunk chunk = world.getChunk(x, z);
        referencesBefore.put(chunk, new HashMap<>(chunk.getStructureReferences()));
        chunk.addStructureReference(structure, homePos.toLong());
      }
    }
  }

  static PlacedStructure place(
      final ServerWorld world, final Structure structure, final BlockBox box) {
    return new PlacedStructure(world, structure, box);
  }

  StructureStart start() {
    return start;
  }

  void remove() {
    home.setStructureStarts(startsBefore);
    referencesBefore.forEach((chunk, references) -> chunk.setStructureReferences(references));
  }

  private static final class Marker extends StructurePiece {

    Marker(final BlockBox box) {
      super(StructurePieceType.JIGSAW, 0, box);
    }

    @Override
    protected void writeNbt(final StructureContext context, final NbtCompound nbt) {}

    @Override
    public void generate(
        final StructureWorldAccess world,
        final StructureAccessor structures,
        final ChunkGenerator generator,
        final Random random,
        final BlockBox chunkBox,
        final ChunkPos chunk,
        final BlockPos pivot) {}
  }
}
