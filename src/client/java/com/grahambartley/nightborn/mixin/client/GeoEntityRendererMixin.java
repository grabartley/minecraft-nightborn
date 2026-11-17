package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.client.name.NameSight;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Mixin(GeoEntityRenderer.class)
abstract class GeoEntityRendererMixin<T extends Entity> {

  @ModifyReturnValue(method = "hasLabel(Lnet/minecraft/entity/Entity;)Z", at = @At("RETURN"))
  private boolean nightborn$nameInSight(final boolean shown, final T entity) {
    return NameSight.shows(entity, shown);
  }
}
