package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.feeding.BiteRules;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.LivingEntity;
import net.minecraft.predicate.entity.EntityPredicates;

public final class Prey {

  private Prey() {}

  public static boolean isPrey(
      final boolean warmBlood, final boolean vampire, final boolean untouchablePlayer) {
    return BiteRules.drinkable(warmBlood, vampire) && !untouchablePlayer;
  }

  public static boolean hasDrinkableBlood(final LivingEntity target) {
    return BiteRules.drinkable(
        BloodQuality.of(target.getType()).isPresent(), Vampires.isVampire(target));
  }

  public static boolean isPrey(final LivingEntity target) {
    return hasDrinkableBlood(target) && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(target);
  }

  public static boolean spared(
      final boolean peaceful, final boolean player, final boolean ownHouseThrall) {
    return peaceful && player || ownHouseThrall;
  }

  public static boolean mayBite(
      final boolean downedOrEscaping,
      final boolean vampireVictim,
      final boolean prey,
      final boolean provoked) {
    return !downedOrEscaping && !vampireVictim && (prey || provoked);
  }
}
