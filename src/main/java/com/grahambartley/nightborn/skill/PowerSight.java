package com.grahambartley.nightborn.skill;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class PowerSight {

  private PowerSight() {}

  @Nullable
  public static LivingEntity looked(final LivingEntity caster, final double range) {
    final Vec3d eye = caster.getEyePos();
    final Vec3d reach = caster.getRotationVec(1.0F).multiply(range);
    final EntityHitResult hit =
        ProjectileUtil.raycast(
            caster,
            eye,
            eye.add(reach),
            caster.getBoundingBox().stretch(reach).expand(1.0),
            entity -> entity instanceof LivingEntity && !entity.isSpectator() && entity.canHit(),
            range * range);
    return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
  }

  @Nullable
  public static LivingEntity aimed(
      final LivingEntity caster, final double range, final CastAim aim) {
    final LivingEntity candidate =
        caster instanceof MobEntity ? aim.subject(caster) : looked(caster, range);
    return candidate != null
            && candidate.isAlive()
            && caster.squaredDistanceTo(candidate) <= range * range
            && caster.canSee(candidate)
            && PowerReach.touches(caster, candidate)
        ? candidate
        : null;
  }
}
