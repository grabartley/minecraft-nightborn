package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.vampire.VampireTicker;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PhantomEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {

  @Inject(method = "tick", at = @At("TAIL"))
  private void nightborn$tickVampire(final CallbackInfo ci) {
    final LivingEntity self = (LivingEntity) (Object) this;
    if (!self.getWorld().isClient && Vampires.isVampire(self)) {
      VampireTicker.tick(self);
    }
  }

  @Inject(
      method = "canTarget(Lnet/minecraft/entity/LivingEntity;)Z",
      at = @At("HEAD"),
      cancellable = true)
  private void nightborn$phantomsIgnoreVampires(
      final LivingEntity target, final CallbackInfoReturnable<Boolean> cir) {
    if ((Object) this instanceof PhantomEntity && Vampires.isVampire(target)) {
      cir.setReturnValue(false);
    }
  }
}
