package com.grahambartley.nightborn.eldest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class EldestArmorTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = ArmorItem.Type.class,
      names = {"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"})
  @DisplayName("the Eldest's armour protects exactly as netherite does")
  void protectsLikeNetherite(final ArmorItem.Type type) {
    final ArmorMaterial eldest = EldestArmor.MATERIAL.value();
    final ArmorMaterial netherite = ArmorMaterials.NETHERITE.value();
    assertEquals(netherite.getProtection(type), eldest.getProtection(type));
    assertEquals(netherite.toughness(), eldest.toughness());
    assertEquals(netherite.knockbackResistance(), eldest.knockbackResistance());
  }

  @Test
  @DisplayName("the material is registered, enchants well and is mended with the ash of the Eldest")
  void registered() {
    assertEquals(
        "nightborn:eldest",
        Registries.ARMOR_MATERIAL.getId(EldestArmor.MATERIAL.value()).toString());
    assertTrue(
        EldestArmor.MATERIAL.value().enchantability()
            > ArmorMaterials.NETHERITE.value().enchantability());
    assertTrue(
        EldestArmor.MATERIAL
            .value()
            .repairIngredient()
            .get()
            .test(new ItemStack(EldestSpoils.ASH)));
  }

  @ParameterizedTest(name = "layer {0}")
  @ValueSource(ints = {1, 2})
  @DisplayName("the Eldest's armour wears its own textures, and both layers ship")
  void wearsItsOwnLayers(final int layer) {
    final ArmorMaterial eldest = EldestArmor.MATERIAL.value();
    assertEquals(1, eldest.layers().size());
    assertEquals(
        "nightborn:textures/models/armor/eldest_layer_" + layer + ".png",
        eldest.layers().get(0).getTexture(layer == 2).toString());
    assertTrue(
        Files.exists(
            Path.of(
                "src/main/resources/assets/nightborn/textures/models/armor/eldest_layer_"
                    + layer
                    + ".png")));
  }
}
