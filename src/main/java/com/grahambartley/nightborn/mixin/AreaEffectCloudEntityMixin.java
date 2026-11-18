package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.accessory.WornClouds;
import com.grahambartley.nightborn.blood.PotionWard;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AreaEffectCloudEntity.class)
abstract class AreaEffectCloudEntityMixin {

  @WrapOperation(
      method = "tick",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z"))
  private boolean nightborn$wardPotion(
      final LivingEntity target,
      final StatusEffectInstance effect,
      final Entity source,
      final Operation<Boolean> original) {
    return original.call(
        target, WornClouds.shorten(target, PotionWard.ward(target, effect)), source);
  }
}
