package com.grahambartley.nightborn.decor;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public final class FacingShapes {

  private FacingShapes() {}

  public static Map<Direction, VoxelShape> fromNorth(final VoxelShape north) {
    final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
    for (final Direction facing : Direction.Type.HORIZONTAL) {
      VoxelShape turned = VoxelShapes.empty();
      for (final Box box : north.getBoundingBoxes()) {
        turned = VoxelShapes.union(turned, VoxelShapes.cuboid(turn(box, facing)));
      }
      shapes.put(facing, turned.simplify());
    }
    return Map.copyOf(shapes);
  }

  public static Vec3d turn(final Vec3d northPoint, final Direction facing) {
    final double x = northPoint.x - 0.5;
    final double z = northPoint.z - 0.5;
    return switch (facing) {
      case SOUTH -> new Vec3d(0.5 - x, northPoint.y, 0.5 - z);
      case WEST -> new Vec3d(0.5 + z, northPoint.y, 0.5 - x);
      case EAST -> new Vec3d(0.5 - z, northPoint.y, 0.5 + x);
      default -> northPoint;
    };
  }

  private static Box turn(final Box box, final Direction facing) {
    final Vec3d a = turn(new Vec3d(box.minX, box.minY, box.minZ), facing);
    final Vec3d b = turn(new Vec3d(box.maxX, box.maxY, box.maxZ), facing);
    return new Box(a, b);
  }
}
