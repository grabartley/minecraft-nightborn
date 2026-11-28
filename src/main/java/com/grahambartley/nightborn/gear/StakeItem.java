package com.grahambartley.nightborn.gear;

import net.minecraft.block.BlockState;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class StakeItem extends ToolItem {

  static final float MATERIAL_SHARE = 0.5F;
  static final float ATTACK_SPEED = -2.0F;

  public StakeItem(final ToolMaterial material, final Settings settings) {
    super(material, settings.attributeModifiers(modifiers(material)));
  }

  static float damage(final ToolMaterial material) {
    return material.getAttackDamage() * MATERIAL_SHARE;
  }

  private static AttributeModifiersComponent modifiers(final ToolMaterial material) {
    return AttributeModifiersComponent.builder()
        .add(
            EntityAttributes.GENERIC_ATTACK_DAMAGE,
            new EntityAttributeModifier(
                BASE_ATTACK_DAMAGE_MODIFIER_ID,
                damage(material),
                EntityAttributeModifier.Operation.ADD_VALUE),
            AttributeModifierSlot.MAINHAND)
        .add(
            EntityAttributes.GENERIC_ATTACK_SPEED,
            new EntityAttributeModifier(
                BASE_ATTACK_SPEED_MODIFIER_ID,
                ATTACK_SPEED,
                EntityAttributeModifier.Operation.ADD_VALUE),
            AttributeModifierSlot.MAINHAND)
        .build();
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
