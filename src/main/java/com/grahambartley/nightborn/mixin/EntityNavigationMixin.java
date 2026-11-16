package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.levitation.LevitationLift;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityNavigation.class)
abstract class EntityNavigationMixin {

  @Shadow @Final protected MobEntity entity;

  @ModifyExpressionValue(
      method = "continueFollowingPath",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;getY()D"))
  private double nightborn$reachStepsWhileLevitating(final double feetY) {
    return LevitationLift.pathFeetY(feetY, Levitation.isLevitating(entity));
  }
}
