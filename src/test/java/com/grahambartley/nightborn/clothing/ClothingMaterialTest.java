package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ClothingMaterialTest {

  private static final ArmorMaterial LEATHER = ArmorMaterials.LEATHER.value();

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = ArmorItem.Type.class,
      names = {"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"})
  @DisplayName("cloth protects like leather in every slot")
  void protectsLikeLeather(final ArmorItem.Type type) {
    assertEquals(LEATHER.getProtection(type), ClothingMaterial.CLOTH.value().getProtection(type));
  }

  @Test
  @DisplayName("cloth enchants, sounds and resists like leather")
  void behavesLikeLeather() {
    final ArmorMaterial cloth = ClothingMaterial.CLOTH.value();
    assertEquals(LEATHER.enchantability(), cloth.enchantability());
    assertEquals(LEATHER.equipSound(), cloth.equipSound());
    assertEquals(LEATHER.toughness(), cloth.toughness());
    assertEquals(LEATHER.knockbackResistance(), cloth.knockbackResistance());
    assertEquals("nightborn:cloth", Registries.ARMOR_MATERIAL.getId(cloth).toString());
  }

  @ParameterizedTest(name = "{0}: leather mends it {1}")
  @CsvSource({"CLOTH, false", "LEATHER, true"})
  @DisplayName("leather mends only the leather pieces")
  void leatherMendsLeather(final ClothingFabric fabric, final boolean mends) {
    assertEquals(
        mends,
        ClothingMaterial.of(fabric)
            .value()
            .repairIngredient()
            .get()
            .test(new ItemStack(Items.LEATHER)));
  }
}
