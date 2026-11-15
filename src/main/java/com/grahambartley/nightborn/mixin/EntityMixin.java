package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.levitation.LevitationRules;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
abstract class EntityMixin {

  @ModifyExpressionValue(
      method =
          "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isOnGround()Z"))
  private boolean nightborn$levitatingStepsUp(final boolean onGround) {
    return (Object) this instanceof LivingEntity living
        ? LevitationRules.movesAsGrounded(onGround, Levitation.isLevitating(living))
        : onGround;
  }
}
