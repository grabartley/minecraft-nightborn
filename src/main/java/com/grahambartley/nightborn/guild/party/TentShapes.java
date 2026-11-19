package com.grahambartley.nightborn.guild.party;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public final class TentShapes {

  private record Box(double x1, double y1, double z1, double x2, double y2, double z2) {}

  private static final List<Box> RIGHT_SIDE =
      List.of(
          new Box(0, 0, 0, 8, 16, 16), new Box(8, 0, 0, 12, 10, 16), new Box(12, 0, 0, 16, 5, 16));
  private static final List<Box> LEFT_SIDE =
      List.of(
          new Box(8, 0, 0, 16, 16, 16), new Box(4, 0, 0, 8, 10, 16), new Box(0, 0, 0, 4, 5, 16));
  private static final List<Box> BACK_WALL = List.of(new Box(0, 0, 14, 16, 16, 16));
  private static final List<Box> ROOF =
      List.of(
          new Box(0, 0, 0, 16, 3, 16), new Box(3, 0, 0, 13, 8, 16), new Box(6, 0, 0, 10, 14, 16));

  private static final Map<Direction, Map<TentPart, VoxelShape>> OUTLINES =
      new EnumMap<>(Direction.class);
  private static final Map<Direction, Map<TentPart, VoxelShape>> COLLISIONS =
      new EnumMap<>(Direction.class);

  static {
    for (final Direction facing : Direction.Type.HORIZONTAL) {
      final Map<TentPart, VoxelShape> outlines = new EnumMap<>(TentPart.class);
      final Map<TentPart, VoxelShape> collisions = new EnumMap<>(TentPart.class);
      for (final TentPart part : TentPart.values()) {
        final VoxelShape outline = shape(boxes(part), facing);
        outlines.put(part, outline);
        collisions.put(part, part.roof() ? VoxelShapes.empty() : outline);
      }
      OUTLINES.put(facing, outlines);
      COLLISIONS.put(facing, collisions);
    }
  }

  private TentShapes() {}

  public static VoxelShape outline(final TentPart part, final Direction facing) {
    return OUTLINES.get(facing).get(part);
  }

  public static VoxelShape collision(final TentPart part, final Direction facing) {
    return COLLISIONS.get(facing).get(part);
  }

  private static List<Box> boxes(final TentPart part) {
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

  private static VoxelShape shape(final List<Box> boxes, final Direction facing) {
    VoxelShape shape = VoxelShapes.empty();
    for (final Box box : boxes) {
      shape = VoxelShapes.union(shape, turn(box, facing));
    }
    return shape.simplify();
  }

  private static VoxelShape turn(final Box box, final Direction facing) {
    final Direction right = facing.rotateYClockwise();
    final Direction back = facing.getOpposite();
    final double ax = x(box.x1(), box.z1(), right, back);
    final double az = z(box.x1(), box.z1(), right, back);
    final double bx = x(box.x2(), box.z2(), right, back);
    final double bz = z(box.x2(), box.z2(), right, back);
    return Block.createCuboidShape(
        Math.min(ax, bx), box.y1(), Math.min(az, bz), Math.max(ax, bx), box.y2(), Math.max(az, bz));
  }

  private static double x(
      final double x, final double z, final Direction right, final Direction back) {
    return 8 + right.getOffsetX() * (x - 8) + back.getOffsetX() * (z - 8);
  }

  private static double z(
      final double x, final double z, final Direction right, final Direction back) {
    return 8 + right.getOffsetZ() * (x - 8) + back.getOffsetZ() * (z - 8);
  }
}
