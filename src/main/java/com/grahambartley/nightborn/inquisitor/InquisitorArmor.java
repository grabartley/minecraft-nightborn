package com.grahambartley.nightborn.inquisitor;

import com.grahambartley.nightborn.boss.SetArmorMaterial;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntry;

public final class InquisitorArmor {

  static final int DURABILITY = 40;
  static final int ENCHANTABILITY = 20;

  public static final RegistryEntry<ArmorMaterial> MATERIAL =
      SetArmorMaterial.register(
          "inquisitor", ENCHANTABILITY, () -> Ingredient.ofItems(InquisitorSpoils.MAIL));

  private InquisitorArmor() {}
}
