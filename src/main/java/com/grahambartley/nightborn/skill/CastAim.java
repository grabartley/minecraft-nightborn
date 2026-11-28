package com.grahambartley.nightborn.skill;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public record CastAim(@Nullable LivingEntity entity, @Nullable Vec3d spot) {

  public static final CastAim OWN_TARGET = new CastAim(null, null);

  public static CastAim at(final LivingEntity entity) {
    return new CastAim(entity, null);
  }

  public static CastAim toward(final Vec3d spot) {
    return new CastAim(null, spot);
  }

  public LivingEntity helped(final LivingEntity caster) {
    return entity == null ? caster : entity;
  }

  @Nullable
  public LivingEntity subject(final LivingEntity caster) {
    if (entity != null || spot != null) {
      return entity;
    }
    return caster instanceof MobEntity mob ? mob.getTarget() : null;
  }

  public Vec3d heading(final LivingEntity caster) {
    if (!(caster instanceof MobEntity)) {
      return caster.getRotationVector();
    }
    if (spot != null) {
      return spot.subtract(caster.getPos());
    }
    final LivingEntity aimed = subject(caster);
    return aimed != null && aimed != caster
        ? aimed.getPos().subtract(caster.getPos())
        : caster.getRotationVector();
  }
}
