package com.grahambartley.nightborn.guild.party;

import com.grahambartley.nightborn.decor.FacingShapes;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public final class TentShapes {

  private static final VoxelShape RIGHT_SIDE =
      VoxelShapes.union(
          Block.createCuboidShape(0, 0, 0, 8, 16, 16),
          Block.createCuboidShape(8, 0, 0, 12, 10, 16),
          Block.createCuboidShape(12, 0, 0, 16, 5, 16));
  private static final VoxelShape LEFT_SIDE =
      VoxelShapes.union(
          Block.createCuboidShape(8, 0, 0, 16, 16, 16),
          Block.createCuboidShape(4, 0, 0, 8, 10, 16),
          Block.createCuboidShape(0, 0, 0, 4, 5, 16));
  private static final VoxelShape BACK_WALL = Block.createCuboidShape(0, 0, 14, 16, 16, 16);
  private static final VoxelShape ROOF =
      VoxelShapes.union(
          Block.createCuboidShape(0, 0, 0, 4, 7, 16),
          Block.createCuboidShape(4, 6, 0, 6, 10, 16),
          Block.createCuboidShape(6, 9, 0, 10, 14, 16),
          Block.createCuboidShape(10, 6, 0, 12, 10, 16),
          Block.createCuboidShape(12, 0, 0, 16, 7, 16));

  private static final Map<TentPart, Map<Direction, VoxelShape>> OUTLINES =
      new EnumMap<>(TentPart.class);

  static {
    for (final TentPart part : TentPart.values()) {
      OUTLINES.put(part, FacingShapes.fromNorth(north(part)));
    }
  }

  private TentShapes() {}

  public static VoxelShape outline(final TentPart part, final Direction facing) {
    return OUTLINES.get(part).get(facing);
  }

  public static VoxelShape collision(final TentPart part, final Direction facing) {
    return part.roof() ? VoxelShapes.empty() : outline(part, facing);
  }

  private static VoxelShape north(final TentPart part) {
    if (part.roof()) {
      return ROOF;
    }
    if (part.right() > 0) {
      return RIGHT_SIDE;
    }
    if (part.right() < 0) {
      return LEFT_SIDE;
    }
    return BACK_WALL;
  }
}
