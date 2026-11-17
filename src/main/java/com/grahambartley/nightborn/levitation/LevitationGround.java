package com.grahambartley.nightborn.levitation;

import com.grahambartley.nightborn.movement.MoveHeading;
import java.util.ArrayList;
import java.util.List;
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
    return sample(entity, box, box.minY + RAY_START_ABOVE_FEET, SEARCH_DEPTH).surface();
  }

  public static OptionalDouble surfaceToFollow(final Entity entity, final Vec3d movementInput) {
    final OptionalDouble below = surfaceBelow(entity);
    final Optional<MoveHeading> heading =
        MoveHeading.of(movementInput.x, movementInput.z, entity.getYaw());
    if (heading.isEmpty()) {
      return below;
    }
    final Box box = entity.getBoundingBox();
    final double startY = LevitationLookAhead.climbLimit(below, box.minY);
    final List<LevitationLookAhead.Sample> samples = new ArrayList<>();
    for (final double distance :
        LevitationLookAhead.distances(entity.getVelocity().horizontalLength())) {
      samples.add(
          sample(
              entity,
              box.offset(heading.get().x() * distance, 0.0, heading.get().z() * distance),
              startY,
              LevitationLookAhead.AHEAD_DEPTH));
    }
    return LevitationLookAhead.groundToFollow(below, samples);
  }

  private static LevitationLookAhead.Sample sample(
      final Entity entity, final Box footprint, final double startY, final double depth) {
    final double[] xs = {
      footprint.minX + FOOTPRINT_INSET, footprint.getCenter().x, footprint.maxX - FOOTPRINT_INSET
    };
    final double[] zs = {
      footprint.minZ + FOOTPRINT_INSET, footprint.getCenter().z, footprint.maxZ - FOOTPRINT_INSET
    };
    boolean blocked = false;
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
        if (hit.getType() == HitResult.Type.MISS) {
          continue;
        }
        if (hit instanceof BlockHitResult block && block.isInsideBlock()) {
          blocked = true;
          continue;
        }
        highest = LevitationLookAhead.higher(highest, OptionalDouble.of(hit.getPos().y));
      }
    }
    return new LevitationLookAhead.Sample(blocked, highest);
  }
}
