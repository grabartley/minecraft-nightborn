package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.batform.BatForm;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
abstract class HeldItemRendererMixin {

  @Inject(
      method =
          "renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V",
      at = @At("HEAD"),
      cancellable = true)
  private void nightborn$noHandsAsABat(
      final float tickDelta,
      final MatrixStack matrices,
      final VertexConsumerProvider.Immediate vertexConsumers,
      final ClientPlayerEntity player,
      final int light,
      final CallbackInfo ci) {
    if (BatForm.isBat(player)) {
      ci.cancel();
    }
  }
}
