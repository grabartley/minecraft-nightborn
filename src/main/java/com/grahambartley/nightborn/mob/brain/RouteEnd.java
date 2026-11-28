package com.grahambartley.nightborn.mob.brain;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class RouteEnd {

  private RouteEnd() {}

  @Nullable
  public static Vec3d of(final MobEntity mob) {
    return of(mob.getNavigation().getCurrentPath());
  }

  @Nullable
  public static Vec3d of(@Nullable final Path path) {
    if (!live(path) || path.getEnd() == null) {
      return null;
    }
    return Vec3d.ofBottomCenter(path.getEnd().getBlockPos());
  }

  @Nullable
  public static Vec3d goal(final MobEntity mob) {
    return goal(mob.getNavigation().getCurrentPath());
  }

  @Nullable
  public static Vec3d goal(@Nullable final Path path) {
    return live(path) ? Vec3d.ofBottomCenter(path.getTarget()) : null;
  }

  public static Route route(final MobEntity mob) {
    return route(
        mob.getPos(),
        mob.getNavigation().getCurrentPath(),
        mob.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE));
  }

  @Nullable
  public static Vec3d course(final MobEntity mob) {
    return course(
        mob.getTarget() == null && mob.hasPositionTarget() && !mob.isInWalkTargetRange()
            ? Vec3d.ofBottomCenter(mob.getPositionTarget())
            : null,
        goal(mob));
  }

  @Nullable
  public static Vec3d course(@Nullable final Vec3d homeAway, @Nullable final Vec3d routeGoal) {
    return homeAway != null ? homeAway : routeGoal;
  }

  public static Route route(final Vec3d from, @Nullable final Path path, final double searchRange) {
    if (!live(path)) {
      return Route.UNKNOWN;
    }
    final List<Vec3d> nodes = new ArrayList<>(path.getLength() - path.getCurrentNodeIndex());
    for (int i = path.getCurrentNodeIndex(); i < path.getLength(); i++) {
      nodes.add(Vec3d.ofBottomCenter(path.getNodePos(i)));
    }
    return Route.of(
        from, nodes, Vec3d.ofBottomCenter(path.getTarget()), path.reachesTarget(), searchRange);
  }

  public static double distance(final Vec3d from, @Nullable final Vec3d end) {
    return end == null ? Double.NaN : from.distanceTo(end);
  }

  private static boolean live(@Nullable final Path path) {
    return path != null && !path.isFinished();
  }
}
