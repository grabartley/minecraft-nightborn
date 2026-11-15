package com.grahambartley.nightborn.clothing;

import com.grahambartley.nightborn.Nightborn;
import java.util.List;
import java.util.Map;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

public final class ClothingMaterial {

  public static final RegistryEntry<ArmorMaterial> CLOTH = register();

  private ClothingMaterial() {}

  public static RegistryEntry<ArmorMaterial> of(final ClothingFabric fabric) {
    return switch (fabric) {
      case CLOTH -> CLOTH;
      case LEATHER -> ArmorMaterials.LEATHER;
    };
  }

  private static RegistryEntry<ArmorMaterial> register() {
    final ArmorMaterial leather = ArmorMaterials.LEATHER.value();
    return Registry.registerReference(
        Registries.ARMOR_MATERIAL,
        Identifier.of(Nightborn.MOD_ID, "cloth"),
        new ArmorMaterial(
            Map.copyOf(leather.defense()),
            leather.enchantability(),
            leather.equipSound(),
            () -> Ingredient.fromTag(ItemTags.WOOL),
            List.of(),
            leather.toughness(),
            leather.knockbackResistance()));
  }
}
