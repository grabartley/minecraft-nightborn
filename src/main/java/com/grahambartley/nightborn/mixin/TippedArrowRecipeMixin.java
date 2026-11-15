package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.holy.HolyWater;
import net.minecraft.recipe.TippedArrowRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TippedArrowRecipe.class)
abstract class TippedArrowRecipeMixin {

  @Inject(
      method =
          "matches(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/world/World;)Z",
      at = @At("RETURN"),
      cancellable = true)
  private void nightborn$holyWaterTipsNoArrows(
      final CraftingRecipeInput input,
      final World world,
      final CallbackInfoReturnable<Boolean> cir) {
    if (cir.getReturnValueZ() && HolyWater.isHolyWater(input.getStackInSlot(1, 1))) {
      cir.setReturnValue(false);
    }
  }
}
