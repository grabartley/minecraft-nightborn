package com.grahambartley.nightborn.particle;

import java.util.Optional;
import java.util.OptionalDouble;
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
  static final double SMALLEST_SHARE = 0.4;

  private DecalSurface() {}

  public static OptionalDouble fit(
      final BlockView world, final Vec3d centre, final Direction face, final double halfExtent) {
    if (holds(world, centre, face, halfExtent)) {
      return OptionalDouble.of(halfExtent);
    }
    final double room = roomInCell(centre, face.getAxis());
    return room >= halfExtent * SMALLEST_SHARE && holds(world, centre, face, room)
        ? OptionalDouble.of(room)
        : OptionalDouble.empty();
  }

  public static boolean holds(
      final BlockView world, final Vec3d centre, final Direction face, final double halfExtent) {
    if (!holdsAt(world, centre, face)) {
      return false;
    }
    final Direction.Axis[] plane = planeAxes(face.getAxis());
    final double a = centre.getComponentAlongAxis(plane[0]);
    final double b = centre.getComponentAlongAxis(plane[1]);
    for (int cellA = firstCell(a, halfExtent); cellA <= lastCell(a, halfExtent); cellA++) {
      for (int cellB = firstCell(b, halfExtent); cellB <= lastCell(b, halfExtent); cellB++) {
        final double[] point = {centre.x, centre.y, centre.z};
        point[plane[0].ordinal()] = cellA + 0.5;
        point[plane[1].ordinal()] = cellB + 0.5;
        if (!holdsAt(world, new Vec3d(point[0], point[1], point[2]), face)) {
          return false;
        }
      }
    }
    return true;
  }

  public static boolean holdsAt(final BlockView world, final Vec3d point, final Direction face) {
    if (face == Direction.DOWN) {
      return false;
    }
    final Vec3d outward = Vec3d.of(face.getVector()).multiply(TOLERANCE);
    final BlockPos support = BlockPos.ofFloored(point.subtract(outward));
    final BlockPos space = BlockPos.ofFloored(point.add(outward));
    if (!world.getFluidState(support).isEmpty() || !world.getFluidState(space).isEmpty()) {
      return false;
    }
    final VoxelShape supportShape = shapeAt(world, support);
    if (supportShape.isEmpty()) {
      return false;
    }
    final Direction.Axis axis = face.getAxis();
    final boolean positive = face.getDirection() == Direction.AxisDirection.POSITIVE;
    final double plane = point.getComponentAlongAxis(axis);
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
      for (final Box box : shapeAt(world, space).getBoundingBoxes()) {
        if (crosses(box, axis, positive, plane)) {
          cover = VoxelShapes.union(cover, flatten(box, axis, support));
        }
      }
    }
    final VoxelShape exposed = VoxelShapes.combine(faceArea, cover, BooleanBiFunction.ONLY_FIRST);
    return !VoxelShapes.matchesAnywhere(
        VoxelShapes.fullCube(), exposed, BooleanBiFunction.ONLY_FIRST);
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

  public static double halfExtent(final double halfSize, final float spin) {
    return halfSize * (Math.abs(Math.cos(spin)) + Math.abs(Math.sin(spin)));
  }

  static double roomInCell(final Vec3d centre, final Direction.Axis normal) {
    double room = 0.5;
    for (final Direction.Axis axis : planeAxes(normal)) {
      final double coordinate = centre.getComponentAlongAxis(axis);
      final double inCell = coordinate - Math.floor(coordinate);
      room = Math.min(room, Math.min(inCell, 1.0 - inCell));
    }
    return room;
  }

  private static int firstCell(final double coordinate, final double halfExtent) {
    return (int) Math.floor(coordinate - halfExtent + TOLERANCE);
  }

  private static int lastCell(final double coordinate, final double halfExtent) {
    return (int) Math.floor(coordinate + halfExtent - TOLERANCE);
  }

  private static Direction.Axis[] planeAxes(final Direction.Axis normal) {
    return switch (normal) {
      case X -> new Direction.Axis[] {Direction.Axis.Y, Direction.Axis.Z};
      case Y -> new Direction.Axis[] {Direction.Axis.X, Direction.Axis.Z};
      case Z -> new Direction.Axis[] {Direction.Axis.X, Direction.Axis.Y};
    };
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
