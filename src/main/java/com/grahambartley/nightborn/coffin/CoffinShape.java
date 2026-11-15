package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.decor.FacingShapes;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.enums.BedPart;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public final class CoffinShape {

  public static final int HEIGHT = 10;

  record Band(int fromZ, int toZ, int fromX, int toX) {}

  static final List<Band> BANDS =
      List.of(
          new Band(0, 2, 4, 12),
          new Band(2, 5, 3, 13),
          new Band(5, 8, 2, 14),
          new Band(8, 12, 1, 15),
          new Band(12, 17, 2, 14),
          new Band(17, 22, 3, 13),
          new Band(22, 27, 4, 12),
          new Band(27, 32, 5, 11));

  private static final Map<Direction, VoxelShape> HEAD = FacingShapes.fromNorth(cell(0));
  private static final Map<Direction, VoxelShape> FOOT = FacingShapes.fromNorth(cell(16));

  private CoffinShape() {}

  public static VoxelShape of(final BedPart part, final Direction facing) {
    return (part == BedPart.HEAD ? HEAD : FOOT).get(facing);
  }

  static VoxelShape cell(final int startZ) {
    VoxelShape shape = VoxelShapes.empty();
    for (final Band band : BANDS) {
      final int from = Math.max(band.fromZ(), startZ) - startZ;
      final int to = Math.min(band.toZ(), startZ + 16) - startZ;
      if (from < to) {
        shape =
            VoxelShapes.union(
                shape, Block.createCuboidShape(band.fromX(), 0, from, band.toX(), HEIGHT, to));
      }
    }
    return shape.simplify();
  }
}
