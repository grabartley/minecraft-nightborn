package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.client.formview.FormViewPass;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {

  @Inject(
      method = "render",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/Framebuffer;beginWrite(Z)V"))
  private void nightborn$drawFormView(
      final RenderTickCounter tickCounter, final boolean tick, final CallbackInfo ci) {
    FormViewPass.draw(tickCounter.getTickDelta(false));
  }
}
