package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.vampire.NightbornDamage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public final class Staking {

  private Staking() {}

  public static boolean isStakeBlow(final DamageSource source) {
    return isStakeStrike(source) || VampireBane.ammo(source).isIn(VampireBane.STAKE_AMMO);
  }

  static boolean isStakeStrike(final DamageSource source) {
    return VampireBane.meleeWith(source, VampireBane.STAKES);
  }

  public static boolean finishesVampire(final DamageSource source) {
    return NightbornDamage.killsVampires(source) || isStakeBlow(source);
  }

  public static boolean staked(final DamageSource source) {
    return source.isIn(NightbornDamage.STAKES) || isStakeBlow(source);
  }

  public static void stake(final LivingEntity vampire, final DamageSource blow) {
    final Entity hunter = blow.getAttacker();
    vampire.damage(NightbornDamage.stake(vampire.getWorld(), hunter), Float.MAX_VALUE);
    final ItemStack weapon = blow.getWeaponStack();
    if (weapon != null && isStakeStrike(blow) && hunter instanceof PlayerEntity wielder) {
      weapon.damage(1, wielder, EquipmentSlot.MAINHAND);
    }
  }
}
