package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class SetArmorMaterialTest {

  @ParameterizedTest(name = "{0} at enchantability {1}, piece {2}")
  @CsvSource({
    "eldest, 20, HELMET",
    "inquisitor, 18, CHESTPLATE",
    "anyone, 5, LEGGINGS",
    "anyone, 30, BOOTS"
  })
  @DisplayName(
      "a set's armour protects as netherite does, wears its own layer and mends with its own"
          + " ingredient")
  void likeNetherite(final String name, final int enchantability, final ArmorItem.Type type) {
    final ArmorMaterial material =
        SetArmorMaterial.likeNetherite(
            name, enchantability, () -> Ingredient.ofItems(Items.AMETHYST_SHARD));
    final ArmorMaterial netherite = ArmorMaterials.NETHERITE.value();
    assertEquals(netherite.getProtection(type), material.getProtection(type));
    assertEquals(netherite.toughness(), material.toughness());
    assertEquals(netherite.knockbackResistance(), material.knockbackResistance());
    assertEquals(enchantability, material.enchantability());
    assertEquals(
        "nightborn:textures/models/armor/" + name + "_layer_1.png",
        material.layers().getFirst().getTexture(false).toString());
    assertTrue(material.repairIngredient().get().test(new ItemStack(Items.AMETHYST_SHARD)));
  }
}
