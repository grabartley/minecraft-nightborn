package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.client.anim.VampirePlayerClips;
import com.grahambartley.nightborn.occult.MistForm;
import com.grahambartley.nightborn.render.BatFormRenderers;
import com.grahambartley.nightborn.render.MistFormRenderer;
import com.grahambartley.nightborn.render.VampirePlayerRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
abstract class PlayerEntityRendererMixin {

  @Unique private VampirePlayerRenderer nightborn$vampire;
  @Unique private BatFormRenderers nightborn$batForm;
  @Unique private MistFormRenderer nightborn$mist;

  @Inject(method = "<init>", at = @At("RETURN"))
  private void nightborn$buildVampireRenderer(
      final EntityRendererFactory.Context context, final boolean slim, final CallbackInfo ci) {
    nightborn$vampire = new VampirePlayerRenderer(context, slim);
    nightborn$batForm = new BatFormRenderers(context);
    nightborn$mist = new MistFormRenderer(context);
  }

  @Inject(
      method =
          "render(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
      at = @At("HEAD"),
      cancellable = true)
  private void nightborn$playVampireClips(
      final AbstractClientPlayerEntity player,
      final float yaw,
      final float tickDelta,
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light,
      final CallbackInfo ci) {
    if (MistForm.on(player)) {
      nightborn$mist.render(player, yaw, tickDelta, matrices, vertexConsumers, light);
      ci.cancel();
    } else if (BatForm.isBat(player)) {
      nightborn$batForm.render(player, yaw, tickDelta, matrices, vertexConsumers, light);
      ci.cancel();
    } else if (VampirePlayerClips.clip(player).isPresent()) {
      nightborn$vampire.render(player, yaw, tickDelta, matrices, vertexConsumers, light);
      ci.cancel();
    }
  }
}
