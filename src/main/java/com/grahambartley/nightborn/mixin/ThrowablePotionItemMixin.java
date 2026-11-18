package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.accessory.WornRefill;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ThrowablePotionItem.class)
abstract class ThrowablePotionItemMixin {

  @Inject(method = "use", at = @At("HEAD"))
  private void nightborn$rememberThrown(
      final World world,
      final PlayerEntity user,
      final Hand hand,
      final CallbackInfoReturnable<TypedActionResult<ItemStack>> cir,
      @Share("thrown") final LocalRef<ItemStack> thrown) {
    thrown.set(user.getStackInHand(hand).copy());
  }

  @Inject(method = "use", at = @At("RETURN"))
  private void nightborn$refillFromBelt(
      final World world,
      final PlayerEntity user,
      final Hand hand,
      final CallbackInfoReturnable<TypedActionResult<ItemStack>> cir,
      @Share("thrown") final LocalRef<ItemStack> thrown) {
    WornRefill.refill(user, hand, thrown.get());
  }
}
