package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.accessory.FrostWard;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PowderSnowBlock.class)
abstract class PowderSnowBlockMixin {

  @ModifyReturnValue(method = "canWalkOnPowderSnow", at = @At("RETURN"))
  private static boolean nightborn$frostWardWalks(final boolean walks, final Entity entity) {
    return walks || FrostWard.warded(entity);
  }
}
