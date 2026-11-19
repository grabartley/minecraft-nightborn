package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.accessory.WornDamage;
import com.grahambartley.nightborn.accessory.WornStrikes;
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
    if (entity.getWorld().isClient) {
      return amount;
    }
    if (amount <= 0.0F) {
      return BloodTithe.share(entity, amount);
    }
    final float worn = WornDamage.scale(entity, source, WornStrikes.scale(entity, source, amount));
    final float formed = MistForm.harm(entity, source, worn * WingedForm.damageTaken(entity));
    final float kept = BloodTithe.share(entity, formed);
    final float taken = floored(amount, kept);
    if (taken > kept) {
      BloodTithe.settle(entity, formed, taken);
    }
    return taken;
  }

  public static float floored(final float original, final float reduced) {
    return Math.max(reduced, original * LEAST_SHARE);
  }
}
