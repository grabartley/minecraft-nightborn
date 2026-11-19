package com.grahambartley.nightborn.trap;

import com.grahambartley.nightborn.hold.Holds;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public final class PinnedBlows {

  private PinnedBlows() {}

  public static float raise(final float amount, final double bonus) {
    return bonus > 0.0 ? (float) (amount * (1.0 + bonus)) : amount;
  }

  public static float scale(
      final LivingEntity target, final DamageSource source, final float amount) {
    if (!(source.getAttacker() instanceof LivingEntity attacker)
        || !Vampires.isVampire(target)
        || !Holds.pinned(target)) {
      return amount;
    }
    return raise(amount, TrapGifts.pinnedDamage(HeldTrapGifts.of(attacker)));
  }
}
