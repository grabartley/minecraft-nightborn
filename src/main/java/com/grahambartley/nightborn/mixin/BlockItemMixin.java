package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.raid.HouseNoise;
import com.grahambartley.nightborn.raid.NoiseAct;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
abstract class BlockItemMixin {

  @Inject(
      method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
      at = @At("RETURN"))
  private void nightborn$placingMakesNoise(
      final ItemPlacementContext context, final CallbackInfoReturnable<ActionResult> cir) {
    if (cir.getReturnValue().isAccepted()
        && context.getPlayer() instanceof ServerPlayerEntity player) {
      HouseNoise.make(player, NoiseAct.PLACE);
    }
  }
}
