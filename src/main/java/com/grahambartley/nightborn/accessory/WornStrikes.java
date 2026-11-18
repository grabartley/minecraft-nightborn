package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.gear.VampireBane;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;

public final class WornStrikes {

  private WornStrikes() {}

  public static float scale(
      final LivingEntity target, final DamageSource source, final float amount) {
    if (amount <= 0.0F
        || target.getWorld().isClient()
        || !(source.getAttacker() instanceof LivingEntity striker)) {
      return amount;
    }
    final boolean blow =
        source.getSource() == striker
            && (source.isOf(DamageTypes.PLAYER_ATTACK) || source.isOf(DamageTypes.MOB_ATTACK));
    final ItemStack held = striker.getMainHandStack();
    return strike(
        amount,
        blow && held.isEmpty(),
        GiftAttributes.value(striker, GiftAttributes.BARE_HAND_DAMAGE),
        blow && held.isIn(VampireBane.STAKES),
        GiftAttributes.value(striker, GiftAttributes.STAKE_DAMAGE),
        HouseRally.factor(striker));
  }

  public static float strike(
      final float amount,
      final boolean bareHanded,
      final double bareExtra,
      final boolean stake,
      final double stakeFactor,
      final double rally) {
    double dealt = amount;
    if (bareHanded) {
      dealt += Math.max(0.0, bareExtra);
    }
    if (stake) {
      dealt *= Math.max(0.0, stakeFactor);
    }
    return (float) (dealt * Math.max(0.0, rally));
  }
}
