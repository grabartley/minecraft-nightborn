package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.coating.Coatings;
import com.grahambartley.nightborn.gear.VampireBane;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
abstract class EnchantmentHelperMixin {

  @Inject(method = "getDamage", at = @At("RETURN"), cancellable = true)
  private static void nightborn$baneOfVampires(
      final ServerWorld world,
      final ItemStack stack,
      final Entity target,
      final DamageSource source,
      final float baseDamage,
      final CallbackInfoReturnable<Float> cir) {
    cir.setReturnValue(VampireBane.strike(stack, target, cir.getReturnValueF()));
  }

  @Inject(method = "getAmmoUse", at = @At("RETURN"), cancellable = true)
  private static void nightborn$coatedAmmoIsAlwaysSpent(
      final ServerWorld world,
      final ItemStack weapon,
      final ItemStack projectile,
      final int baseAmmoUse,
      final CallbackInfoReturnable<Integer> cir) {
    if (Coatings.isCoatedAmmo(projectile)) {
      cir.setReturnValue(Math.max(cir.getReturnValueI(), baseAmmoUse));
    }
  }
}
