package com.grahambartley.nightborn.mob.brain;

import net.minecraft.entity.ai.pathing.Path;
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
    if (path == null || path.isFinished() || path.getEnd() == null) {
      return null;
    }
    return Vec3d.ofBottomCenter(path.getEnd().getBlockPos());
  }

  public static double distance(final Vec3d from, @Nullable final Vec3d end) {
    return end == null ? Double.NaN : from.distanceTo(end);
  }
}
