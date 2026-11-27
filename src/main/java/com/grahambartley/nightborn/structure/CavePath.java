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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public final class CavePath extends StructurePiece {

  private static final String FLOORS = "floors";
  private static final String SILL = "sill";
  private static final String HEADROOM = "headroom";
  private static final String LANES = "lanes";
  private static final String SIDE = "side";

  private final List<BlockPos> floors;
  private final BlockState sill;
  private final CaveRoom room;
  private final Direction side;

  public CavePath(
      final List<BlockPos> floors,
      final BlockState sill,
      final CaveRoom room,
      final Direction side) {
    super(NightbornStructures.CAVE_PATH, 0, room.around(floors, side));
    this.floors = List.copyOf(floors);
    this.sill = sill;
    this.room = room;
    this.side = side;
  }

  public CavePath(final StructureContext context, final NbtCompound nbt) {
    super(NightbornStructures.CAVE_PATH, nbt);
    this.floors = Arrays.stream(nbt.getLongArray(FLOORS)).mapToObj(BlockPos::fromLong).toList();
    this.sill =
        NbtHelper.toBlockState(
            context.registryManager().getWrapperOrThrow(RegistryKeys.BLOCK), nbt.getCompound(SILL));
    this.room =
        nbt.contains(HEADROOM)
            ? new CaveRoom(nbt.getInt(HEADROOM), nbt.getInt(LANES))
            : CaveRoom.PERSON;
    final Direction saved = Direction.byName(nbt.getString(SIDE));
    this.side = saved != null ? saved : Direction.NORTH;
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
    nbt.putInt(HEADROOM, room.headroom());
    nbt.putInt(LANES, room.lanes());
    nbt.putString(SIDE, side.asString());
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
    for (final BlockPos floor :
        floors.stream().flatMap(route -> room.floors(route, side).stream()).toList()) {
      if (!chunkBox.contains(floor)) {
        continue;
      }
      if (needsFloor(world.getBlockState(floor))) {
        world.setBlockState(floor, sill, Block.NOTIFY_LISTENERS);
      }
      for (int up = 1; up <= room.headroom(); up++) {
        final BlockPos space = floor.up(up);
        if (needsClearing(world.getBlockState(space))) {
          world.setBlockState(space, Blocks.CAVE_AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
        }
      }
    }
  }
}
