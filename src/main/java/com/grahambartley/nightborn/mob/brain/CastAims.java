package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.skill.CastAim;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class CastAims {

  private CastAims() {}

  public static Optional<CastAim> resolve(final MobEntity caster, final Aim aim) {
    return resolve(aim, caster, aim == Aim.SPOT ? RouteEnd.of(caster) : null);
  }

  static Optional<CastAim> resolve(
      final Aim aim, final LivingEntity self, @Nullable final Vec3d routeEnd) {
    return switch (aim) {
      case SELF -> Optional.of(CastAim.at(self));
      case SPOT -> Optional.ofNullable(routeEnd).map(CastAim::toward);
      case TARGET, ALLY, ALLIES, FOES -> Optional.of(CastAim.OWN_TARGET);
    };
  }
}
