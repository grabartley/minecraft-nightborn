package com.grahambartley.nightborn.mob.combat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import org.jetbrains.annotations.Nullable;

public final class Grudge {

  @Nullable private LivingEntity attacker;

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

  public void provoke(final LivingEntity by) {
    attacker = by;
  }

  public boolean held() {
    return attacker != null;
  }

  public boolean against(@Nullable final LivingEntity target) {
    return attacker != null && attacker == target;
  }

  public void forgive(@Nullable final LivingEntity target) {
    if (against(target)) {
      attacker = null;
    }
  }

  public void update(final MobEntity holder, final boolean downed) {
    if (attacker != null
        && !holds(
            attacker.isAlive()
                && attacker.getWorld() == holder.getWorld()
                && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(attacker),
            against(holder.getTarget()) || holder.getAttacker() == attacker,
            downed,
            holder.distanceTo(attacker),
            holder.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE))) {
      attacker = null;
    }
  }
}
