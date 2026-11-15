package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.GearSettings;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class VampireBane {

  public static final TagKey<Item> STAKES =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(Nightborn.MOD_ID, "stakes"));
  public static final TagKey<Item> SILVER_WEAPONS =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(Nightborn.MOD_ID, "silver_weapons"));

  private VampireBane() {}

  public static float bonus(final ItemStack weapon, final Entity target) {
    return target instanceof LivingEntity living
        ? bonus(
            Vampires.isVampire(living),
            weapon.isIn(STAKES),
            weapon.isIn(SILVER_WEAPONS),
            ServerConfigService.current().gear())
        : 0.0F;
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
}
