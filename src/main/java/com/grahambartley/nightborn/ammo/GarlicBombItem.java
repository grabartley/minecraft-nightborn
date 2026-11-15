package com.grahambartley.nightborn.ammo;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ProjectileItem;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

public class GarlicBombItem extends Item implements CrossbowAmmo, ProjectileItem {

  public GarlicBombItem(final Item.Settings settings) {
    super(settings);
  }

  @Override
  public ProjectileEntity createProjectile(
      final World world,
      final LivingEntity shooter,
      final ItemStack ammo,
      final ItemStack crossbow,
      final boolean critical) {
    final GarlicBombEntity bomb = new GarlicBombEntity(world, shooter);
    bomb.setItem(ammo.copyWithCount(1));
    return bomb;
  }

  @Override
  public ProjectileEntity createEntity(
      final World world, final Position pos, final ItemStack stack, final Direction direction) {
    final GarlicBombEntity bomb = new GarlicBombEntity(world, pos.getX(), pos.getY(), pos.getZ());
    bomb.setItem(stack.copyWithCount(1));
    return bomb;
  }
}
