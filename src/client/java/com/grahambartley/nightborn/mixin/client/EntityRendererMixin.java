package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.render.CoffinSleeperView;
import com.grahambartley.nightborn.vanish.VanishAttachment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
abstract class EntityRendererMixin<T extends Entity> {

  @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
  private void nightborn$hideTheUnseen(
      final T entity,
      final Frustum frustum,
      final double x,
      final double y,
      final double z,
      final CallbackInfoReturnable<Boolean> cir) {
    if (VanishAttachment.isGone(entity)
        || (entity instanceof LivingEntity sleeper && CoffinSleeperView.hidden(sleeper))) {
      cir.setReturnValue(false);
    }
  }
}
