package com.grahambartley.nightborn.ward;

import com.grahambartley.nightborn.Nightborn;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.chunk.WorldChunk;

public final class CrucifixShapes extends PersistentState {

  private static final String DATA_NAME = Nightborn.MOD_ID + "_crucifix_shapes";
  private static final String CENTRES = "centres";

  private final Long2ObjectOpenHashMap<LongSet> byChunk = new Long2ObjectOpenHashMap<>();

  public static CrucifixShapes get(final ServerWorld world) {
    return world
        .getPersistentStateManager()
        .getOrCreate(
            new PersistentState.Type<>(CrucifixShapes::new, CrucifixShapes::fromNbt, null),
            DATA_NAME);
  }

  public static boolean flipsMaterial(final BlockState before, final BlockState after) {
    return before.isIn(WardTags.CRUCIFIX_MATERIALS) != after.isIn(WardTags.CRUCIFIX_MATERIALS);
  }

  public void refreshAround(final ServerWorld world, final BlockPos changed) {
    for (final BlockPos centre : CrucifixShape.centresTouching(changed)) {
      if (loadedAround(world, centre)) {
        set(centre, CrucifixShape.centredAt(centre, pos -> isMaterial(world, pos)));
      }
    }
  }

  public List<BlockPos> near(final ServerWorld world, final BlockPos from, final int range) {
    final List<BlockPos> found = new ArrayList<>();
    final double rangeSquared = (double) range * range;
    final int minChunkX = ChunkSectionPos.getSectionCoord(from.getX() - range);
    final int maxChunkX = ChunkSectionPos.getSectionCoord(from.getX() + range);
    final int minChunkZ = ChunkSectionPos.getSectionCoord(from.getZ() - range);
    final int maxChunkZ = ChunkSectionPos.getSectionCoord(from.getZ() + range);
    for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
      for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
        final LongSet centres = byChunk.get(ChunkPos.toLong(chunkX, chunkZ));
        if (centres == null) {
          continue;
        }
        for (final long packed : centres.toLongArray()) {
          final BlockPos centre = BlockPos.fromLong(packed);
          if (centre.getSquaredDistance(from) > rangeSquared || !loadedAround(world, centre)) {
            continue;
          }
          if (CrucifixShape.centredAt(centre, pos -> isMaterial(world, pos))) {
            found.add(centre);
          } else {
            set(centre, false);
          }
        }
      }
    }
    return found;
  }

  public boolean contains(final BlockPos centre) {
    final LongSet centres = byChunk.get(ChunkPos.toLong(centre));
    return centres != null && centres.contains(centre.asLong());
  }

  private void set(final BlockPos centre, final boolean present) {
    final long chunk = ChunkPos.toLong(centre);
    if (present) {
      if (put(centre)) {
        markDirty();
      }
      return;
    }
    final LongSet centres = byChunk.get(chunk);
    if (centres != null && centres.remove(centre.asLong())) {
      if (centres.isEmpty()) {
        byChunk.remove(chunk);
      }
      markDirty();
    }
  }

  private boolean put(final BlockPos centre) {
    return byChunk
        .computeIfAbsent(ChunkPos.toLong(centre), key -> new LongOpenHashSet())
        .add(centre.asLong());
  }

  private static boolean isMaterial(final ServerWorld world, final BlockPos pos) {
    final WorldChunk chunk =
        world
            .getChunkManager()
            .getWorldChunk(
                ChunkSectionPos.getSectionCoord(pos.getX()),
                ChunkSectionPos.getSectionCoord(pos.getZ()));
    return chunk != null && chunk.getBlockState(pos).isIn(WardTags.CRUCIFIX_MATERIALS);
  }

  private static boolean loadedAround(final ServerWorld world, final BlockPos centre) {
    final int reach = CrucifixShape.REACH;
    return loaded(world, centre.getX() - reach, centre.getZ() - reach)
        && loaded(world, centre.getX() + reach, centre.getZ() - reach)
        && loaded(world, centre.getX() - reach, centre.getZ() + reach)
        && loaded(world, centre.getX() + reach, centre.getZ() + reach);
  }

  private static boolean loaded(final ServerWorld world, final int x, final int z) {
    return world
            .getChunkManager()
            .getWorldChunk(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z))
        != null;
  }

  @Override
  public NbtCompound writeNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registryLookup) {
    final LongOpenHashSet all = new LongOpenHashSet();
    byChunk.values().forEach(all::addAll);
    nbt.putLongArray(CENTRES, all.toLongArray());
    return nbt;
  }

  static CrucifixShapes fromNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registryLookup) {
    final CrucifixShapes shapes = new CrucifixShapes();
    for (final long packed : nbt.getLongArray(CENTRES)) {
      shapes.put(BlockPos.fromLong(packed));
    }
    return shapes;
  }
}
