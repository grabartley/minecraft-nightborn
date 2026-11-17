package com.grahambartley.nightborn.eldest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.gear.SilverMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.tag.BlockTags;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class AshenSilverTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(AshenSilver.class)
  @DisplayName("ashen silver outlasts and out-hits silver and iron, and stays under netherite")
  void sitsAboveSilver(final AshenSilver material) {
    assertTrue(material.getDurability() > SilverMaterial.SILVER.getDurability());
    assertTrue(material.getDurability() > ToolMaterials.IRON.getDurability());
    assertTrue(material.getDurability() < ToolMaterials.NETHERITE.getDurability());
    assertTrue(material.getAttackDamage() > SilverMaterial.SILVER.getAttackDamage());
    assertTrue(material.getAttackDamage() <= ToolMaterials.DIAMOND.getAttackDamage());
    assertTrue(material.getEnchantability() > SilverMaterial.SILVER.getEnchantability());
    assertEquals(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, material.getInverseTag());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(AshenSilver.class)
  @DisplayName("ashen silver is mended with the ash of the Eldest, not with silver")
  void repairsWithAsh(final AshenSilver material) {
    assertTrue(material.getRepairIngredient().test(new ItemStack(EldestSpoils.ASH)));
    assertFalse(material.getRepairIngredient().test(new ItemStack(GearItems.SILVER_INGOT)));
  }
}
