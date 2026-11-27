package com.grahambartley.nightborn.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record CaveRoom(int headroom, int lanes) {

  public static final int LOWEST = 2;
  public static final int TALLEST = 6;
  public static final int WIDEST = 3;
  public static final CaveRoom PERSON = new CaveRoom(LOWEST, 1);

  public static final Codec<CaveRoom> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.intRange(LOWEST, TALLEST)
                          .fieldOf("headroom")
                          .forGetter(CaveRoom::headroom),
                      Codec.intRange(1, WIDEST).fieldOf("lanes").forGetter(CaveRoom::lanes))
                  .apply(instance, CaveRoom::new));

  public List<BlockPos> floors(final BlockPos floor, final Direction side) {
    final List<BlockPos> floors = new ArrayList<>(lanes);
    for (int lane = 0; lane < lanes; lane++) {
      floors.add(floor.offset(side, lane));
    }
    return floors;
  }

  public List<BlockPos> carved(
      final List<BlockPos> route, final Direction side, final BlockBox lair) {
    final Set<BlockPos> carved = new LinkedHashSet<>(route);
    for (final BlockPos floor : route) {
      for (final BlockPos lane : floors(floor, side)) {
        if (clearOf(lane, lair)) {
          carved.add(lane);
        }
      }
    }
    return List.copyOf(carved);
  }

  public BlockBox around(final List<BlockPos> floors) {
    return BlockBox.encompassPositions(floors)
        .map(
            box ->
                new BlockBox(
                    box.getMinX(),
                    box.getMinY(),
                    box.getMinZ(),
                    box.getMaxX(),
                    box.getMaxY() + headroom,
                    box.getMaxZ()))
        .orElseThrow(() -> new IllegalArgumentException("A cave path needs at least one floor"));
  }

  private boolean clearOf(final BlockPos floor, final BlockBox lair) {
    for (int up = 0; up <= headroom; up++) {
      if (lair.contains(floor.up(up))) {
        return false;
      }
    }
    return true;
  }
}
