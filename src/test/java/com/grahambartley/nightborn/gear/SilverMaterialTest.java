package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.tag.BlockTags;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class SilverMaterialTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(SilverMaterial.class)
  @DisplayName("silver is softer than iron, tougher than gold, hits as hard as iron")
  void sitsBetweenGoldAndIron(final SilverMaterial material) {
    assertTrue(material.getDurability() > ToolMaterials.GOLD.getDurability());
    assertTrue(material.getDurability() < ToolMaterials.IRON.getDurability());
    assertEquals(ToolMaterials.IRON.getAttackDamage(), material.getAttackDamage());
    assertEquals(BlockTags.INCORRECT_FOR_IRON_TOOL, material.getInverseTag());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(SilverMaterial.class)
  @DisplayName("silver tools are mended with silver ingots")
  void repairsWithSilver(final SilverMaterial material) {
    assertTrue(material.getRepairIngredient().test(new ItemStack(GearItems.SILVER_INGOT)));
    assertTrue(!material.getRepairIngredient().test(new ItemStack(Items.IRON_INGOT)));
  }
}
