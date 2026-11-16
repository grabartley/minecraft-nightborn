package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.mind.MindFeatures;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
abstract class MobEntityMixin {

  @Inject(method = "tickNewAi", at = @At("HEAD"), cancellable = true)
  private void nightborn$standStillWhileMesmerised(final CallbackInfo ci) {
    final MobEntity self = (MobEntity) (Object) this;
    if (MindFeatures.stilled(self)) {
      MindFeatures.holdStill(self);
      ci.cancel();
    }
  }
}
