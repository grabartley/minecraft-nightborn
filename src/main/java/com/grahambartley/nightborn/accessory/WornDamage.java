package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.core.NightSight;
import com.grahambartley.nightborn.sun.SunFire;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class WornDamage {

  public static final TagKey<EntityType<?>> HUNTER_SHOTS =
      TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Nightborn.MOD_ID, "hunter_shots"));

  private WornDamage() {}

  public static float scale(
      final LivingEntity target, final DamageSource source, final float amount) {
    if (amount <= 0.0F || target.getWorld().isClient()) {
      return amount;
    }
    final Entity direct = source.getSource();
    final boolean fire =
        unsunnedFire(
            source.isIn(DamageTypeTags.IS_FIRE),
            source.isOf(DamageTypes.ON_FIRE),
            SunFire.burning(target));
    return GiftScale.amount(
        amount,
        factor(
            NightSight.isNight(target.getWorld()),
            GiftAttributes.value(target, GiftAttributes.NIGHT_DAMAGE),
            direct != null && direct.getType().isIn(HUNTER_SHOTS),
            GiftAttributes.value(target, GiftAttributes.HUNTER_SHOT_DAMAGE),
            fire,
            GiftAttributes.value(target, GiftAttributes.FIRE_DAMAGE)));
  }

  public static boolean unsunnedFire(
      final boolean fire, final boolean burning, final boolean sunLit) {
    return fire && !(burning && sunLit);
  }

  public static double factor(
      final boolean night,
      final double nightFactor,
      final boolean hunterShot,
      final double shotFactor,
      final boolean unsunnedFire,
      final double fireFactor) {
    double factor = 1.0;
    if (night) {
      factor *= nightFactor;
    }
    if (hunterShot) {
      factor *= shotFactor;
    }
    if (unsunnedFire) {
      factor *= fireFactor;
    }
    return factor;
  }
}
