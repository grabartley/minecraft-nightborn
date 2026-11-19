package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.sun.Parasols;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Slot.class)
abstract class SlotMixin {

  @Inject(method = "setStack(Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"))
  private void nightborn$aParasolPutAwayFolds(final ItemStack stack, final CallbackInfo ci) {
    Parasols.fold(stack);
  }
}
