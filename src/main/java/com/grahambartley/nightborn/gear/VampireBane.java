package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.coating.CoatedStrikes;
import com.grahambartley.nightborn.config.GearSettings;
import com.grahambartley.nightborn.enchant.EnchantedValues;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class VampireBane {

  public static final TagKey<Item> STAKES = tag("stakes");
  public static final TagKey<Item> SILVER_WEAPONS = tag("silver_weapons");
  public static final TagKey<Item> STAKE_AMMO = tag("stake_ammo");
  public static final TagKey<Item> SILVER_AMMO = tag("silver_ammo");

  private VampireBane() {}

  public static float bonus(final ItemStack weapon, final Entity target) {
    return bonus(weapon, target, STAKES, SILVER_WEAPONS)
        + CoatedStrikes.weaponBonus(weapon, target);
  }

  public static float strike(final ItemStack weapon, final Entity target, final float damage) {
    final float total = damage + bonus(weapon, target);
    return seeksTheHeart(weapon.isIn(STAKES), Vampires.isVampire(target))
        ? EnchantedValues.apply(weapon, EnchantedValues.STAKE_DAMAGE, total)
        : total;
  }

  public static boolean seeksTheHeart(final boolean stake, final boolean vampire) {
    return stake && vampire;
  }

  public static float ammoBonus(final ItemStack ammo, final Entity target) {
    return bonus(ammo, target, STAKE_AMMO, SILVER_AMMO) + CoatedStrikes.ammoBonus(ammo, target);
  }

  public static double projectileBase(final double base, final float bonus, final double speed) {
    return speed > 0.0 ? base + bonus / speed : base;
  }

  public static float bonus(
      final boolean vampire,
      final boolean stake,
      final boolean silver,
      final GearSettings settings) {
    if (!vampire) {
      return 0.0F;
    }
    return (stake ? settings.stakeDamage() : 0) + (silver ? settings.silverDamage() : 0);
  }

  public static boolean isSilverBlow(final DamageSource source) {
    return meleeWith(source, SILVER_WEAPONS) || ammo(source).isIn(SILVER_AMMO);
  }

  static boolean meleeWith(final DamageSource source, final TagKey<Item> weapons) {
    final ItemStack weapon = source.getWeaponStack();
    return source.isDirect()
        && (source.isOf(DamageTypes.PLAYER_ATTACK) || source.isOf(DamageTypes.MOB_ATTACK))
        && weapon != null
        && weapon.isIn(weapons);
  }

  public static ItemStack ammo(final DamageSource source) {
    return source.getSource() instanceof PersistentProjectileEntity projectile
        ? projectile.getItemStack()
        : ItemStack.EMPTY;
  }

  private static float bonus(
      final ItemStack stack,
      final Entity target,
      final TagKey<Item> stakes,
      final TagKey<Item> silver) {
    return target instanceof LivingEntity living
        ? bonus(
            Vampires.isVampire(living),
            stack.isIn(stakes),
            stack.isIn(silver),
            ServerConfigService.current().gear())
        : 0.0F;
  }

  private static TagKey<Item> tag(final String path) {
    return TagKey.of(RegistryKeys.ITEM, Identifier.of(Nightborn.MOD_ID, path));
  }
}
