package com.grahambartley.nightborn.eldest;

import com.grahambartley.nightborn.Nightborn;
import java.util.List;
import java.util.Map;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class EldestArmor {

  static final int DURABILITY = 40;
  static final int ENCHANTABILITY = 20;

  public static final RegistryEntry<ArmorMaterial> MATERIAL = register();

  private EldestArmor() {}

  private static RegistryEntry<ArmorMaterial> register() {
    final ArmorMaterial netherite = ArmorMaterials.NETHERITE.value();
    return Registry.registerReference(
        Registries.ARMOR_MATERIAL,
        Identifier.of(Nightborn.MOD_ID, "eldest"),
        new ArmorMaterial(
            Map.copyOf(netherite.defense()),
            ENCHANTABILITY,
            netherite.equipSound(),
            () -> Ingredient.ofItems(EldestSpoils.ASH),
            List.of(new ArmorMaterial.Layer(Identifier.ofVanilla("netherite"))),
            netherite.toughness(),
            netherite.knockbackResistance()));
  }
}
