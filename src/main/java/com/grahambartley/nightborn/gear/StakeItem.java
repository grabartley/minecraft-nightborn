package com.grahambartley.nightborn.gear;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StakeItem extends ToolItem {

  static final int BASE_DAMAGE = 2;
  static final float ATTACK_SPEED = -2.0F;

  public StakeItem(final ToolMaterial material, final Settings settings) {
    super(
        material,
        settings.attributeModifiers(
            SwordItem.createAttributeModifiers(material, BASE_DAMAGE, ATTACK_SPEED)));
  }

  @Override
  public boolean canMine(
      final BlockState state, final World world, final BlockPos pos, final PlayerEntity miner) {
    return !miner.isCreative();
  }

  @Override
  public boolean postHit(
      final ItemStack stack, final LivingEntity target, final LivingEntity attacker) {
    return true;
  }

  @Override
  public void postDamageEntity(
      final ItemStack stack, final LivingEntity target, final LivingEntity attacker) {
    stack.damage(1, attacker, EquipmentSlot.MAINHAND);
  }
}
