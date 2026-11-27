package com.grahambartley.nightborn.particle;

import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public final class DecalSurface {

  static final double TOLERANCE = 1.0e-3;

  private DecalSurface() {}

  static boolean holdsAt(final BlockView world, final Vec3d point, final Direction face) {
    if (face == Direction.DOWN) {
      return false;
    }
    final Vec3d outward = Vec3d.of(face.getVector()).multiply(TOLERANCE);
    final BlockPos support = BlockPos.ofFloored(point.subtract(outward));
    final BlockPos space = BlockPos.ofFloored(point.add(outward));
    if (!world.getFluidState(support).isEmpty() || !world.getFluidState(space).isEmpty()) {
      return false;
    }
    final VoxelShape localShape = world.getBlockState(support).getCollisionShape(world, support);
    if (localShape.isEmpty()) {
      return false;
    }
    final Direction.Axis axis = face.getAxis();
    final boolean positive = face.getDirection() == Direction.AxisDirection.POSITIVE;
    final double plane = point.getComponentAlongAxis(axis);
    if (Block.isShapeFullCube(localShape)) {
      final double boundary = support.getComponentAlongAxis(axis) + (positive ? 1.0 : 0.0);
      return Math.abs(plane - boundary) <= TOLERANCE
          && coverFrom(world, space, axis, positive, plane, support).isEmpty();
    }
    final VoxelShape supportShape =
        localShape.offset(support.getX(), support.getY(), support.getZ());
    VoxelShape faceArea = VoxelShapes.empty();
    VoxelShape cover = VoxelShapes.empty();
    for (final Box box : supportShape.getBoundingBoxes()) {
      if (endsAt(box, axis, positive, plane)) {
        faceArea = VoxelShapes.union(faceArea, flatten(box, axis, support));
      }
      if (crosses(box, axis, positive, plane)) {
        cover = VoxelShapes.union(cover, flatten(box, axis, support));
      }
    }
    if (!space.equals(support)) {
      cover = VoxelShapes.union(cover, coverFrom(world, space, axis, positive, plane, support));
    }
    return coversCell(VoxelShapes.combine(faceArea, cover, BooleanBiFunction.ONLY_FIRST));
  }

  private static VoxelShape coverFrom(
      final BlockView world,
      final BlockPos space,
      final Direction.Axis axis,
      final boolean positive,
      final double plane,
      final BlockPos support) {
    VoxelShape cover = VoxelShapes.empty();
    for (final Box box : shapeAt(world, space).getBoundingBoxes()) {
      if (crosses(box, axis, positive, plane)) {
        cover = VoxelShapes.union(cover, flatten(box, axis, support));
      }
    }
    return cover;
  }

  private static boolean coversCell(final VoxelShape area) {
    return !VoxelShapes.matchesAnywhere(VoxelShapes.fullCube(), area, BooleanBiFunction.ONLY_FIRST);
  }

  public static Optional<Vec3d> floorBelow(
      final BlockView world, final Vec3d feet, final int reach) {
    final BlockPos start = BlockPos.ofFloored(feet);
    for (int down = 0; down <= reach + 1; down++) {
      final BlockPos ground = start.down(down);
      final VoxelShape shape = world.getBlockState(ground).getCollisionShape(world, ground);
      if (shape.isEmpty()) {
        continue;
      }
      final double top = ground.getY() + shape.getMax(Direction.Axis.Y);
      if (top > feet.y + TOLERANCE) {
        continue;
      }
      final Vec3d floor = new Vec3d(feet.x, top, feet.z);
      return holdsAt(world, floor, Direction.UP) ? Optional.of(floor) : Optional.empty();
    }
    return Optional.empty();
  }

  private static VoxelShape shapeAt(final BlockView world, final BlockPos pos) {
    return world
        .getBlockState(pos)
        .getCollisionShape(world, pos)
        .offset(pos.getX(), pos.getY(), pos.getZ());
  }

  private static boolean endsAt(
      final Box box, final Direction.Axis axis, final boolean positive, final double plane) {
    final double edge = positive ? box.getMax(axis) : box.getMin(axis);
    return Math.abs(edge - plane) <= TOLERANCE;
  }

  private static boolean crosses(
      final Box box, final Direction.Axis axis, final boolean positive, final double plane) {
    return positive
        ? box.getMin(axis) <= plane + TOLERANCE && box.getMax(axis) > plane + TOLERANCE
        : box.getMax(axis) >= plane - TOLERANCE && box.getMin(axis) < plane - TOLERANCE;
  }

  private static VoxelShape flatten(
      final Box box, final Direction.Axis normal, final BlockPos origin) {
    final double[] min = new double[3];
    final double[] max = new double[3];
    for (final Direction.Axis axis : Direction.Axis.values()) {
      final int i = axis.ordinal();
      if (axis == normal) {
        min[i] = 0.0;
        max[i] = 1.0;
        continue;
      }
      final double start = origin.getComponentAlongAxis(axis);
      min[i] = Math.max(0.0, box.getMin(axis) - start);
      max[i] = Math.min(1.0, box.getMax(axis) - start);
      if (max[i] - min[i] <= TOLERANCE) {
        return VoxelShapes.empty();
      }
    }
    return VoxelShapes.cuboid(min[0], min[1], min[2], max[0], max[1], max[2]);
  }
}
