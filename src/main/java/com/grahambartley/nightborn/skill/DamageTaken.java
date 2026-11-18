package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.batform.WingedForm;
import com.grahambartley.nightborn.mind.BloodTithe;
import com.grahambartley.nightborn.occult.MistForm;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public final class DamageTaken {

  public static final float LEAST_SHARE = 0.2F;

  private DamageTaken() {}

  public static float scale(
      final LivingEntity entity, final DamageSource source, final float amount) {
    if (entity.getWorld().isClient || amount <= 0.0F) {
      return amount;
    }
    final float formed = MistForm.harm(entity, source, amount * WingedForm.damageTaken(entity));
    return floored(amount, BloodTithe.share(entity, source, formed));
  }

  public static float floored(final float original, final float reduced) {
    return Math.max(reduced, original * LEAST_SHARE);
  }
}
