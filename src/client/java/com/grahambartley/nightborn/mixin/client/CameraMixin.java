package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.coffin.CoffinSleepers;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Camera.class)
abstract class CameraMixin {

  @ModifyConstant(method = "update", constant = @Constant(floatValue = 0.3F))
  private float nightborn$lookAtTheLidFromInside(
      final float lift, @Local(argsOnly = true) final Entity focused) {
    return focused instanceof LivingEntity sleeper && CoffinSleepers.inCoffin(sleeper)
        ? 0.0F
        : lift;
  }
}
