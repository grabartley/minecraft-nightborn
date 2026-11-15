package com.grahambartley.nightborn.levitation;

import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
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
    return highestUnder(entity, box, box.minY + RAY_START_ABOVE_FEET, SEARCH_DEPTH, false);
  }

  public static OptionalDouble surfaceToFollow(final Entity entity, final Vec3d movementInput) {
    final OptionalDouble below = surfaceBelow(entity);
    final Optional<LevitationLookAhead.Heading> heading =
        LevitationLookAhead.heading(movementInput.x, movementInput.z, entity.getYaw());
    if (heading.isEmpty()) {
      return below;
    }
    final Box box = entity.getBoundingBox();
    final double startY = box.minY + entity.getStepHeight();
    final double depth = entity.getStepHeight() + LevitationLift.HOVER_HEIGHT;
    OptionalDouble highest = below;
    for (final double distance :
        LevitationLookAhead.distances(entity.getVelocity().horizontalLength())) {
      highest =
          LevitationLookAhead.higher(
              highest,
              highestUnder(
                  entity,
                  box.offset(heading.get().x() * distance, 0.0, heading.get().z() * distance),
                  startY,
                  depth,
                  true));
    }
    return highest;
  }

  private static OptionalDouble highestUnder(
      final Entity entity,
      final Box footprint,
      final double startY,
      final double depth,
      final boolean skipBlocked) {
    final double[] xs = {
      footprint.minX + FOOTPRINT_INSET, footprint.getCenter().x, footprint.maxX - FOOTPRINT_INSET
    };
    final double[] zs = {
      footprint.minZ + FOOTPRINT_INSET, footprint.getCenter().z, footprint.maxZ - FOOTPRINT_INSET
    };
    OptionalDouble highest = OptionalDouble.empty();
    for (final double x : xs) {
      for (final double z : zs) {
        final HitResult hit =
            entity
                .getWorld()
                .raycast(
                    new RaycastContext(
                        new Vec3d(x, startY, z),
                        new Vec3d(x, startY - depth, z),
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.ANY,
                        entity));
        if (hit.getType() == HitResult.Type.MISS
            || skipBlocked && hit instanceof BlockHitResult block && block.isInsideBlock()) {
          continue;
        }
        highest = LevitationLookAhead.higher(highest, OptionalDouble.of(hit.getPos().y));
      }
    }
    return highest;
  }
}
