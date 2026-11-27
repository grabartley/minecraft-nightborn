package com.grahambartley.nightborn.particle;

import java.util.OptionalDouble;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;

public final class DecalFootprint {

  static final double SMALLEST_SHARE = 0.4;

  private DecalFootprint() {}

  static OptionalDouble fit(
      final BlockView world, final Vec3d centre, final Direction face, final double halfExtent) {
    if (holds(world, centre, face, halfExtent)) {
      return OptionalDouble.of(halfExtent);
    }
    final double room = roomInCell(centre, face.getAxis());
    return room >= halfExtent * SMALLEST_SHARE && holds(world, centre, face, room)
        ? OptionalDouble.of(room)
        : OptionalDouble.empty();
  }

  public static OptionalDouble fittedHalfSize(
      final BlockView world,
      final Vec3d centre,
      final Direction face,
      final double halfSize,
      final float spin) {
    final double extent = halfExtent(halfSize, spin);
    final OptionalDouble fit = fit(world, centre, face, extent);
    return fit.isPresent() ? OptionalDouble.of(halfSize * fit.getAsDouble() / extent) : fit;
  }

  public static boolean holds(
      final BlockView world, final Vec3d centre, final Direction face, final double halfExtent) {
    final Direction.Axis[] plane = planeAxes(face.getAxis());
    final double a = centre.getComponentAlongAxis(plane[0]);
    final double b = centre.getComponentAlongAxis(plane[1]);
    for (int cellA = firstCell(a, halfExtent); cellA <= lastCell(a, halfExtent); cellA++) {
      for (int cellB = firstCell(b, halfExtent); cellB <= lastCell(b, halfExtent); cellB++) {
        final double[] point = {centre.x, centre.y, centre.z};
        point[plane[0].ordinal()] = cellA + 0.5;
        point[plane[1].ordinal()] = cellB + 0.5;
        if (!DecalSurface.holdsAt(world, new Vec3d(point[0], point[1], point[2]), face)) {
          return false;
        }
      }
    }
    return true;
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
    return (int) Math.floor(coordinate - halfExtent + DecalSurface.TOLERANCE);
  }

  private static int lastCell(final double coordinate, final double halfExtent) {
    return (int) Math.floor(coordinate + halfExtent - DecalSurface.TOLERANCE);
  }

  private static Direction.Axis[] planeAxes(final Direction.Axis normal) {
    return switch (normal) {
      case X -> new Direction.Axis[] {Direction.Axis.Y, Direction.Axis.Z};
      case Y -> new Direction.Axis[] {Direction.Axis.X, Direction.Axis.Z};
      case Z -> new Direction.Axis[] {Direction.Axis.X, Direction.Axis.Y};
    };
  }
}
