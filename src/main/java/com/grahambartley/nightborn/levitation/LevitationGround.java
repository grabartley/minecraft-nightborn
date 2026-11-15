package com.grahambartley.nightborn.levitation;

import java.util.OptionalDouble;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class LevitationGround {

  static final double SEARCH_DEPTH = 48.0;
  static final double RAY_START_ABOVE_FEET = 0.05;
  static final double FOOTPRINT_INSET = 0.05;

  private LevitationGround() {}

  public static OptionalDouble surfaceBelow(final Entity entity) {
    final Box box = entity.getBoundingBox();
    final double startY = box.minY + RAY_START_ABOVE_FEET;
    final double[] xs = {box.minX + FOOTPRINT_INSET, entity.getX(), box.maxX - FOOTPRINT_INSET};
    final double[] zs = {box.minZ + FOOTPRINT_INSET, entity.getZ(), box.maxZ - FOOTPRINT_INSET};
    OptionalDouble highest = OptionalDouble.empty();
    for (final double x : xs) {
      for (final double z : zs) {
        final HitResult hit =
            entity
                .getWorld()
                .raycast(
                    new RaycastContext(
                        new Vec3d(x, startY, z),
                        new Vec3d(x, startY - SEARCH_DEPTH, z),
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.ANY,
                        entity));
        if (hit.getType() != HitResult.Type.MISS
            && (highest.isEmpty() || hit.getPos().y > highest.getAsDouble())) {
          highest = OptionalDouble.of(hit.getPos().y);
        }
      }
    }
    return highest;
  }
}
