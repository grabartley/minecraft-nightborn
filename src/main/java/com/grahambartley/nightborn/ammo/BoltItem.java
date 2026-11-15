package com.grahambartley.nightborn.ammo;

import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ProjectileItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

public class BoltItem extends Item implements CrossbowAmmo, ProjectileItem {

  public BoltItem(final Item.Settings settings) {
    super(settings);
  }

  @Override
  public ProjectileEntity createProjectile(
      final World world,
      final LivingEntity shooter,
      final ItemStack ammo,
      final ItemStack crossbow,
      final boolean critical) {
    final BoltEntity bolt = new BoltEntity(world, shooter, ammo.copyWithCount(1), crossbow);
    bolt.setCritical(critical);
    bolt.setSound(SoundEvents.ITEM_CROSSBOW_HIT);
    return bolt;
  }

  @Override
  public ProjectileEntity createEntity(
      final World world, final Position pos, final ItemStack stack, final Direction direction) {
    final BoltEntity bolt =
        new BoltEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack.copyWithCount(1));
    bolt.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
    return bolt;
  }

  @Override
  public void appendTooltip(
      final ItemStack stack,
      final TooltipContext context,
      final List<Text> tooltip,
      final TooltipType type) {
    final PotionContentsComponent contents = stack.get(DataComponentTypes.POTION_CONTENTS);
    if (contents != null) {
      contents.buildTooltip(tooltip::add, 1.0F, context.getUpdateTickRate());
    }
  }
}
