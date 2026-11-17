package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.downed.DownedTargeting;
import com.grahambartley.nightborn.mind.MindFeatures;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
abstract class MobEntityMixin {

  @Inject(method = "tickNewAi", at = @At("HEAD"))
  private void nightborn$forgetATargetThatGoesDown(final CallbackInfo ci) {
    DownedTargeting.forget((MobEntity) (Object) this);
  }

  @Inject(method = "tickNewAi", at = @At("HEAD"), cancellable = true)
  private void nightborn$standStillWhileMesmerised(final CallbackInfo ci) {
    if (MindFeatures.hold((MobEntity) (Object) this)) {
      ci.cancel();
    }
  }
}
