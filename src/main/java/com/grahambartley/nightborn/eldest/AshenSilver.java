package com.grahambartley.nightborn.eldest;

import net.minecraft.block.Block;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

public enum AshenSilver implements ToolMaterial {
  ASHEN_SILVER;

  static final int DURABILITY = 1100;
  static final float MINING_SPEED = 8.0F;
  static final float ATTACK_DAMAGE = 3.0F;
  static final int ENCHANTABILITY = 20;

  @Override
  public int getDurability() {
    return DURABILITY;
  }

  @Override
  public float getMiningSpeedMultiplier() {
    return MINING_SPEED;
  }

  @Override
  public float getAttackDamage() {
    return ATTACK_DAMAGE;
  }

  @Override
  public TagKey<Block> getInverseTag() {
    return BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
  }

  @Override
  public int getEnchantability() {
    return ENCHANTABILITY;
  }

  @Override
  public Ingredient getRepairIngredient() {
    return Ingredient.ofItems(EldestSpoils.ASH);
  }
}
