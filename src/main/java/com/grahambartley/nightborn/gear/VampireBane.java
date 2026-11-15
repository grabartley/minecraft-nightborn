package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.GearSettings;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
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
    return bonus(weapon, target, STAKES, SILVER_WEAPONS);
  }

  public static float ammoBonus(final ItemStack ammo, final Entity target) {
    return bonus(ammo, target, STAKE_AMMO, SILVER_AMMO);
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
