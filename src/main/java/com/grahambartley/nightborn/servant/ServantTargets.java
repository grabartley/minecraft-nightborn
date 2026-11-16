package com.grahambartley.nightborn.servant;

import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class ServantTargets {

  private ServantTargets() {}

  public static boolean mayAttack(final MobEntity servant, final LivingEntity candidate) {
    return TargetRules.mayAttack(
        candidate.isAlive(),
        Servants.serves(servant, candidate),
        Servants.sameMaster(servant, candidate),
        candidate instanceof PlayerEntity player && (player.isCreative() || player.isSpectator()));
  }

  @Nullable
  public static LivingEntity mastersFoe(
      final MobEntity servant, final Predicate<LivingEntity> allowed) {
    final LivingEntity master = Servants.masterHere(servant);
    if (master != null) {
      if (fits(master.getAttacker(), allowed)) {
        return master.getAttacker();
      }
      if (master instanceof MobEntity mob && fits(mob.getTarget(), allowed)) {
        return mob.getTarget();
      }
      if (TargetRules.ownerStillFighting(master.age, master.getLastAttackTime())
          && fits(master.getAttacking(), allowed)) {
        return master.getAttacking();
      }
    }
    return fits(servant.getAttacker(), allowed) ? servant.getAttacker() : null;
  }

  private static boolean fits(
      @Nullable final LivingEntity candidate, final Predicate<LivingEntity> allowed) {
    return candidate != null && allowed.test(candidate);
  }
}
