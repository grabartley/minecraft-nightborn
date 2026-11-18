package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.client.sight.RevealView;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
abstract class EntityMixin {

  @Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
  private void nightborn$revealColour(final CallbackInfoReturnable<Integer> info) {
    RevealView.colour((Entity) (Object) this).ifPresent(info::setReturnValue);
  }
}
