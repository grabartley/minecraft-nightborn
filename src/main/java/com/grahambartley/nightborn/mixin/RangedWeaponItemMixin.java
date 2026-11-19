package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.accessory.BeltAmmo;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RangedWeaponItem.class)
abstract class RangedWeaponItemMixin {

  @Inject(method = "load", at = @At("RETURN"))
  private static void nightborn$beltAmmoUsed(
      final ItemStack weapon,
      final ItemStack projectile,
      final LivingEntity shooter,
      final CallbackInfoReturnable<List<ItemStack>> cir) {
    BeltAmmo.loaded(shooter, projectile);
  }
}
