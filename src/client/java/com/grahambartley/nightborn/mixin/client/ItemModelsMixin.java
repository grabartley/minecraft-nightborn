package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.render.AmmoModels;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemModels.class)
abstract class ItemModelsMixin {

  @Shadow
  public abstract BakedModelManager getModelManager();

  @Inject(
      method =
          "getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;",
      at = @At("HEAD"),
      cancellable = true)
  private void nightborn$drawHunterAmmo(
      final ItemStack stack, final CallbackInfoReturnable<BakedModel> cir) {
    AmmoModels.modelFor(stack).ifPresent(id -> cir.setReturnValue(getModelManager().getModel(id)));
  }
}
