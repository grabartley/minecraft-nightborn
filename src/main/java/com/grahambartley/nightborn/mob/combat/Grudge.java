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
      final boolean downed,
      final double distance,
      final double followRange) {
    return attackerPresent && !downed && distance <= followRange;
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

  public void tick(final MobEntity holder, final boolean downed) {
    if (attacker != null
        && !holds(
            attacker.isAlive() && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(attacker),
            downed,
            holder.distanceTo(attacker),
            holder.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE))) {
      attacker = null;
    }
  }
}
