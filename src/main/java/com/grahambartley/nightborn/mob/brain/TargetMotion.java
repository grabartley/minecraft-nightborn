package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.skill.MobPowerUse;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class TargetMotion {

  @Nullable private UUID target;
  @Nullable private Vec3d at;
  private long tick;

  public Vec3d sample(final LivingEntity seen, final long now, final int thinkTicks) {
    final Vec3d pos = seen.getPos();
    final Vec3d moved =
        perTick(seen.getUuid().equals(target) ? at : null, tick, pos, now, staleAfter(thinkTicks));
    target = seen.getUuid();
    at = pos;
    tick = now;
    return moved;
  }

  public static long staleAfter(final int thinkTicks) {
    return 2L * thinkTicks + MobPowerUse.MAX_TELL_TICKS;
  }

  public static Vec3d perTick(
      @Nullable final Vec3d from,
      final long fromTick,
      final Vec3d to,
      final long toTick,
      final long staleAfter) {
    final long ticks = toTick - fromTick;
    if (from == null || ticks <= 0 || ticks > staleAfter) {
      return Vec3d.ZERO;
    }
    return new Vec3d((to.x - from.x) / ticks, 0.0, (to.z - from.z) / ticks);
  }
}
