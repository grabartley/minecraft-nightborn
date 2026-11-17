package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.downed.DownedTargeting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WardenEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WardenEntity.class)
abstract class WardenEntityMixin {

  @Inject(method = "isValidTarget", at = @At("HEAD"), cancellable = true)
  private void nightborn$theWardenLeavesTheDownedAlone(
      @Nullable final Entity entity, final CallbackInfoReturnable<Boolean> cir) {
    if (entity instanceof LivingEntity target
        && DownedTargeting.spares((WardenEntity) (Object) this, target)) {
      cir.setReturnValue(false);
    }
  }
}
