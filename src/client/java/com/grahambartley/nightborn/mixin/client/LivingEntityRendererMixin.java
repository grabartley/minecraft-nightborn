package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.client.name.NameSight;
import com.grahambartley.nightborn.client.sight.UnseenBodies;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
abstract class LivingEntityRendererMixin<T extends LivingEntity> {

  @ModifyReturnValue(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("RETURN"))
  private boolean nightborn$nameInSight(final boolean shown, final T entity) {
    return NameSight.shows(entity, shown);
  }

  @ModifyReturnValue(method = "isVisible", at = @At("RETURN"))
  private boolean nightborn$hideTheSheltered(final boolean visible, final T entity) {
    return visible && !UnseenBodies.bodyHidden(entity);
  }
}
