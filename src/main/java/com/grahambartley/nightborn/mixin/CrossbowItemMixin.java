package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.ammo.CrossbowAmmo;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
abstract class CrossbowItemMixin {

  @Inject(
      method = {"getProjectiles", "getHeldProjectiles"},
      at = @At("RETURN"),
      cancellable = true)
  private void nightborn$loadsHunterAmmo(final CallbackInfoReturnable<Predicate<ItemStack>> cir) {
    cir.setReturnValue(cir.getReturnValue().or(CrossbowAmmo::is));
  }

  @Inject(method = "createArrowEntity", at = @At("HEAD"), cancellable = true)
  private void nightborn$firesHunterAmmo(
      final World world,
      final LivingEntity shooter,
      final ItemStack weaponStack,
      final ItemStack projectileStack,
      final boolean critical,
      final CallbackInfoReturnable<ProjectileEntity> cir) {
    if (projectileStack.getItem() instanceof CrossbowAmmo ammo) {
      cir.setReturnValue(
          ammo.createProjectile(world, shooter, projectileStack, weaponStack, critical));
    }
  }
}
