package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.grahambartley.nightborn.blood.PotionWard;
import com.grahambartley.nightborn.holy.HolyWater;
import com.grahambartley.nightborn.holy.SplashReach;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PotionEntity.class)
abstract class PotionEntityMixin {

  @WrapOperation(
      method = "applySplashPotion",
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
    return original.call(target, PotionWard.ward(target, effect), source);
  }

  @ModifyConstant(method = "applySplashPotion", constant = @Constant(doubleValue = 4.0))
  private double nightborn$holyReach(final double reach) {
    return SplashReach.reach(reach, nightborn$holy(), nightborn$extraReach());
  }

  @ModifyConstant(method = "applySplashPotion", constant = @Constant(doubleValue = 16.0))
  private double nightborn$holyReachSquared(final double reachSquared) {
    return SplashReach.reachSquared(reachSquared, nightborn$holy(), nightborn$extraReach());
  }

  @Unique
  private boolean nightborn$holy() {
    return HolyWater.isHolyWater(((PotionEntity) (Object) this).getStack());
  }

  @Unique
  private double nightborn$extraReach() {
    return ((PotionEntity) (Object) this).getOwner() instanceof LivingEntity thrower
        ? GiftAttributes.value(thrower, GiftAttributes.HOLY_WATER_RADIUS)
        : 0.0;
  }
}
