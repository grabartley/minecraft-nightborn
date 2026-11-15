package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.vampire.NightbornDamage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;

public final class Staking {

  private Staking() {}

  public static boolean isStakeBlow(final DamageSource source) {
    final ItemStack weapon = source.getWeaponStack();
    return isStakeBlow(
        source.isDirect()
            && (source.isOf(DamageTypes.PLAYER_ATTACK) || source.isOf(DamageTypes.MOB_ATTACK)),
        weapon != null && weapon.isIn(VampireBane.STAKES));
  }

  public static boolean isStakeBlow(final boolean melee, final boolean stakeInHand) {
    return melee && stakeInHand;
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
    if (weapon != null && hunter instanceof LivingEntity wielder) {
      weapon.damage(1, wielder, EquipmentSlot.MAINHAND);
    }
  }
}
