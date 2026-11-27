package com.grahambartley.nightborn.mob.brain;

import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class TargetMotion {

  static final long STALE_TICKS = 60;

  @Nullable private UUID target;
  @Nullable private Vec3d at;
  private long tick;

  public Vec3d sample(final LivingEntity seen, final long now) {
    final Vec3d pos = seen.getPos();
    final Vec3d moved = perTick(seen.getUuid().equals(target) ? at : null, tick, pos, now);
    target = seen.getUuid();
    at = pos;
    tick = now;
    return moved;
  }

  public static Vec3d perTick(
      @Nullable final Vec3d from, final long fromTick, final Vec3d to, final long toTick) {
    final long ticks = toTick - fromTick;
    if (from == null || ticks <= 0 || ticks > STALE_TICKS) {
      return Vec3d.ZERO;
    }
    return new Vec3d((to.x - from.x) / ticks, 0.0, (to.z - from.z) / ticks);
  }
}
