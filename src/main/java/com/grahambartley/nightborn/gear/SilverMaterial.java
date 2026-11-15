package com.grahambartley.nightborn.gear;

import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

public enum SilverMaterial implements ToolMaterial {
  SILVER(160, 7.0F, 2.0F, 18, () -> Ingredient.ofItems(GearItems.SILVER_INGOT));

  private final int durability;
  private final float miningSpeed;
  private final float attackDamage;
  private final int enchantability;
  private final Supplier<Ingredient> repair;

  SilverMaterial(
      final int durability,
      final float miningSpeed,
      final float attackDamage,
      final int enchantability,
      final Supplier<Ingredient> repair) {
    this.durability = durability;
    this.miningSpeed = miningSpeed;
    this.attackDamage = attackDamage;
    this.enchantability = enchantability;
    this.repair = repair;
  }

  @Override
  public int getDurability() {
    return durability;
  }

  @Override
  public float getMiningSpeedMultiplier() {
    return miningSpeed;
  }

  @Override
  public float getAttackDamage() {
    return attackDamage;
  }

  @Override
  public TagKey<Block> getInverseTag() {
    return BlockTags.INCORRECT_FOR_IRON_TOOL;
  }

  @Override
  public int getEnchantability() {
    return enchantability;
  }

  @Override
  public Ingredient getRepairIngredient() {
    return repair.get();
  }
}
