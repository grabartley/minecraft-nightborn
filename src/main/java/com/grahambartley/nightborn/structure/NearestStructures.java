package com.grahambartley.nightborn.structure;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;

public final class NearestStructures {

  public static final int CHUNK_BLOCKS = 16;

  private NearestStructures() {}

  public record Found(Identifier structure, BlockPos pos) {}

  public static List<Found> eachOf(
      final ServerWorld world, final TagKey<Structure> tag, final BlockPos from, final int range) {
    final List<Found> found = new ArrayList<>();
    world
        .getRegistryManager()
        .get(RegistryKeys.STRUCTURE)
        .getEntryList(tag)
        .ifPresent(
            entries -> {
              for (final RegistryEntry<Structure> entry : entries) {
                final Pair<BlockPos, RegistryEntry<Structure>> hit =
                    world
                        .getChunkManager()
                        .getChunkGenerator()
                        .locateStructure(
                            world, RegistryEntryList.of(entry), from, chunks(range), false);
                if (hit != null) {
                  entry
                      .getKey()
                      .ifPresent(key -> found.add(new Found(key.getValue(), hit.getFirst())));
                }
              }
            });
    return found;
  }

  public static int chunks(final int blocks) {
    return Math.max(1, Math.ceilDiv(blocks, CHUNK_BLOCKS));
  }
}
