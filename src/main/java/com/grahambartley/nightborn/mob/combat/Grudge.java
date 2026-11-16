package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.mob.Prey;
import java.util.function.BiPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import org.jetbrains.annotations.Nullable;

public final class Grudge {

  @Nullable private LivingEntity attacker;

  public static boolean fightsBack(final boolean attackerIsVampire) {
    return !attackerIsVampire;
  }

  public static boolean holds(
      final boolean attackerPresent,
      final boolean engaged,
      final boolean downed,
      final double distance,
      final double followRange) {
    return attackerPresent && engaged && !downed && distance <= followRange;
  }

  public static boolean provoked(final boolean againstTarget, final boolean prey) {
    return againstTarget || !prey;
  }

  public void provoke(final LivingEntity by, final boolean vampire) {
    if (fightsBack(vampire)) {
      attacker = by;
    }
  }

  public boolean held() {
    return attacker != null;
  }

  public boolean against(@Nullable final LivingEntity target) {
    return attacker != null && attacker == target;
  }

  public void hold(
      final MobEntity holder,
      final boolean downed,
      final BiPredicate<LivingEntity, Boolean> dropsTarget) {
    final LivingEntity target = holder.getTarget();
    if (attacker != null
        && !holds(
            attacker.isAlive() && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(attacker),
            against(target) || holder.getAttacker() == attacker,
            downed,
            holder.distanceTo(attacker),
            holder.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE))) {
      attacker = null;
    }
    if (target != null
        && (downed || dropsTarget.test(target, provoked(against(target), Prey.isPrey(target))))) {
      if (against(target)) {
        attacker = null;
      }
      holder.setTarget(null);
    }
  }
}
