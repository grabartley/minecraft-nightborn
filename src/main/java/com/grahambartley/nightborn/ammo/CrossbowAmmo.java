package com.grahambartley.nightborn.ammo;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface CrossbowAmmo {

  ProjectileEntity createProjectile(
      World world, LivingEntity shooter, ItemStack ammo, ItemStack crossbow, boolean critical);

  static boolean is(final ItemStack stack) {
    return stack.getItem() instanceof CrossbowAmmo;
  }
}
