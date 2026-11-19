package com.grahambartley.nightborn.boss;

import com.grahambartley.nightborn.Nightborn;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class SetArmorMaterial {

  private SetArmorMaterial() {}

  public static ArmorMaterial likeNetherite(
      final String name, final int enchantability, final Supplier<Ingredient> repair) {
    final ArmorMaterial netherite = ArmorMaterials.NETHERITE.value();
    return new ArmorMaterial(
        Map.copyOf(netherite.defense()),
        enchantability,
        netherite.equipSound(),
        repair,
        List.of(new ArmorMaterial.Layer(Identifier.of(Nightborn.MOD_ID, name))),
        netherite.toughness(),
        netherite.knockbackResistance());
  }

  public static RegistryEntry<ArmorMaterial> register(
      final String name, final int enchantability, final Supplier<Ingredient> repair) {
    return Registry.registerReference(
        Registries.ARMOR_MATERIAL,
        Identifier.of(Nightborn.MOD_ID, name),
        likeNetherite(name, enchantability, repair));
  }
}
