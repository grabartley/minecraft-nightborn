package com.grahambartley.nightborn.structure;

import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public final class CavePath extends StructurePiece {

  static final int HEADROOM = 2;
  private static final String FLOORS = "floors";
  private static final String SILL = "sill";

  private final List<BlockPos> floors;
  private final BlockState sill;

  public CavePath(final List<BlockPos> floors, final BlockState sill) {
    super(NightbornStructures.CAVE_PATH, 0, around(floors));
    this.floors = List.copyOf(floors);
    this.sill = sill;
  }

  public CavePath(final StructureContext context, final NbtCompound nbt) {
    super(NightbornStructures.CAVE_PATH, nbt);
    this.floors = Arrays.stream(nbt.getLongArray(FLOORS)).mapToObj(BlockPos::fromLong).toList();
    this.sill =
        NbtHelper.toBlockState(
            context.registryManager().getWrapperOrThrow(RegistryKeys.BLOCK), nbt.getCompound(SILL));
  }

  public static BlockBox around(final List<BlockPos> floors) {
    return BlockBox.encompassPositions(floors)
        .map(
            box ->
                new BlockBox(
                    box.getMinX(),
                    box.getMinY(),
                    box.getMinZ(),
                    box.getMaxX(),
                    box.getMaxY() + HEADROOM,
                    box.getMaxZ()))
        .orElseThrow(() -> new IllegalArgumentException("A cave path needs at least one floor"));
  }

  public List<BlockPos> floors() {
    return floors;
  }

  public static boolean needsFloor(final BlockState floor) {
    return CaveCell.of(floor) != CaveCell.SOLID;
  }

  public static boolean needsClearing(final BlockState space) {
    return CaveCell.of(space) != CaveCell.OPEN;
  }

  @Override
  protected void writeNbt(final StructureContext context, final NbtCompound nbt) {
    nbt.putLongArray(FLOORS, floors.stream().mapToLong(BlockPos::asLong).toArray());
    nbt.put(SILL, NbtHelper.fromBlockState(sill));
  }

  @Override
  public void generate(
      final StructureWorldAccess world,
      final StructureAccessor structures,
      final ChunkGenerator generator,
      final Random random,
      final BlockBox chunkBox,
      final ChunkPos chunk,
      final BlockPos pivot) {
    for (final BlockPos floor : floors) {
      if (!chunkBox.contains(floor)) {
        continue;
      }
      if (needsFloor(world.getBlockState(floor))) {
        world.setBlockState(floor, sill, Block.NOTIFY_LISTENERS);
      }
      for (int up = 1; up <= HEADROOM; up++) {
        final BlockPos space = floor.up(up);
        if (needsClearing(world.getBlockState(space))) {
          world.setBlockState(space, Blocks.CAVE_AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
      }
    }
  }
}
