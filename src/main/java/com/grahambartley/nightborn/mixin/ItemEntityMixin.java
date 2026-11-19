package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.sun.Parasols;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
abstract class ItemEntityMixin {

  @Inject(method = "setStack", at = @At("HEAD"))
  private void nightborn$aDroppedParasolFolds(final ItemStack stack, final CallbackInfo ci) {
    Parasols.fold(stack);
  }
}
