package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.decor.FacingShapes;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.enums.BedPart;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public final class CoffinShape {

  public static final int HEIGHT = 12;

  record Band(double fromZ, double toZ, double halfWidth) {}

  static final List<Band> BANDS =
      List.of(
          new Band(0, 2, 9.5),
          new Band(2, 20, 10),
          new Band(20, 22, 9.75),
          new Band(22, 24, 9.25),
          new Band(24, 26, 8.5),
          new Band(26, 28, 8),
          new Band(28, 30, 7.5),
          new Band(30, 32, 7));

  private static final Map<Direction, VoxelShape> HEAD = FacingShapes.fromNorth(cell(0));
  private static final Map<Direction, VoxelShape> FOOT = FacingShapes.fromNorth(cell(16));
  private static final Map<Direction, VoxelShape> STANDING_HEAD =
      FacingShapes.fromNorth(stoodUp(cell(0)));
  private static final Map<Direction, VoxelShape> STANDING_FOOT =
      FacingShapes.fromNorth(stoodUp(cell(16)));

  private CoffinShape() {}

  public static VoxelShape of(final BedPart part, final Direction facing, final boolean standing) {
    if (standing) {
      return (part == BedPart.HEAD ? STANDING_HEAD : STANDING_FOOT).get(facing);
    }
    return (part == BedPart.HEAD ? HEAD : FOOT).get(facing);
  }

  static VoxelShape cell(final int startZ) {
    VoxelShape shape = VoxelShapes.empty();
    for (final Band band : BANDS) {
      final double from = Math.max(band.fromZ(), startZ) - startZ;
      final double to = Math.min(band.toZ(), startZ + 16) - startZ;
      final double fromX = Math.max(0, 8 - band.halfWidth());
      final double toX = Math.min(16, 8 + band.halfWidth());
      if (from < to) {
        shape = VoxelShapes.union(shape, Block.createCuboidShape(fromX, 0, from, toX, HEIGHT, to));
      }
    }
    return shape.simplify();
  }

  static VoxelShape stoodUp(final VoxelShape lying) {
    VoxelShape shape = VoxelShapes.empty();
    for (final Box box : lying.getBoundingBoxes()) {
      shape =
          VoxelShapes.union(
              shape,
              VoxelShapes.cuboid(
                  box.minX, 1.0 - box.maxZ, box.minY, box.maxX, 1.0 - box.minZ, box.maxY));
    }
    return shape.simplify();
  }
}
