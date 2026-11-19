package com.grahambartley.nightborn.inquisitor;

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
class InquisitorArmorTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = ArmorItem.Type.class,
      names = {"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"})
  @DisplayName("the Inquisitor's armour protects exactly as netherite does")
  void protectsLikeNetherite(final ArmorItem.Type type) {
    final ArmorMaterial inquisitor = InquisitorArmor.MATERIAL.value();
    final ArmorMaterial netherite = ArmorMaterials.NETHERITE.value();
    assertEquals(netherite.getProtection(type), inquisitor.getProtection(type));
    assertEquals(netherite.toughness(), inquisitor.toughness());
    assertEquals(netherite.knockbackResistance(), inquisitor.knockbackResistance());
  }

  @Test
  @DisplayName("the material is registered, enchants well and is mended with the Inquisitor's mail")
  void registered() {
    assertEquals(
        "nightborn:inquisitor",
        Registries.ARMOR_MATERIAL.getId(InquisitorArmor.MATERIAL.value()).toString());
    assertTrue(
        InquisitorArmor.MATERIAL.value().enchantability()
            > ArmorMaterials.NETHERITE.value().enchantability());
    assertTrue(
        InquisitorArmor.MATERIAL
            .value()
            .repairIngredient()
            .get()
            .test(new ItemStack(InquisitorSpoils.MAIL)));
  }

  @ParameterizedTest(name = "layer {0}")
  @ValueSource(ints = {1, 2})
  @DisplayName("the Inquisitor's armour wears its own textures, and both layers ship")
  void wearsItsOwnLayers(final int layer) {
    final ArmorMaterial inquisitor = InquisitorArmor.MATERIAL.value();
    assertEquals(1, inquisitor.layers().size());
    assertEquals(
        "nightborn:textures/models/armor/inquisitor_layer_" + layer + ".png",
        inquisitor.layers().get(0).getTexture(layer == 2).toString());
    assertTrue(
        Files.exists(
            Path.of(
                "src/main/resources/assets/nightborn/textures/models/armor/inquisitor_layer_"
                    + layer
                    + ".png")));
  }
}
