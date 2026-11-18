package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.accessory.Cobwebs;
import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CobwebBlock.class)
abstract class CobwebBlockMixin {

  @ModifyArg(
      method = "onEntityCollision",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/entity/Entity;slowMovement(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Vec3d;)V"),
      index = 1)
  private Vec3d nightborn$spiderBrooch(
      final Vec3d slowing, @Local(argsOnly = true) final Entity entity) {
    return entity instanceof LivingEntity living
        ? Cobwebs.slowing(slowing, GiftAttributes.value(living, GiftAttributes.COBWEB_SLOW))
        : slowing;
  }
}
