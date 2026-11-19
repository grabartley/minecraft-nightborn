package com.grahambartley.nightborn.eldest;

import com.grahambartley.nightborn.boss.SetArmorMaterial;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntry;

public final class EldestArmor {

  static final int DURABILITY = 40;
  static final int ENCHANTABILITY = 20;

  public static final RegistryEntry<ArmorMaterial> MATERIAL =
      SetArmorMaterial.register(
          "eldest", ENCHANTABILITY, () -> Ingredient.ofItems(EldestSpoils.ASH));

  private EldestArmor() {}
}
