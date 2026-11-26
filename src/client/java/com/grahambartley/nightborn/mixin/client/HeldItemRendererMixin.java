package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.client.anim.FirstPersonSway;
import com.grahambartley.nightborn.client.anim.PlayerClips;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
abstract class HeldItemRendererMixin {

  @Unique private static final float SHOULDER_X = 0.35F;
  @Unique private static final float SHOULDER_Y = -0.3F;
  @Unique private static final float SHOULDER_Z = 0.2F;

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

  @Inject(
      method = "renderFirstPersonItem",
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/client/util/math/MatrixStack;push()V",
              ordinal = 0,
              shift = At.Shift.AFTER))
  private void nightborn$followPowerClip(
      final AbstractClientPlayerEntity player,
      final float tickDelta,
      final float pitch,
      final Hand hand,
      final float swingProgress,
      final ItemStack item,
      final float equipProgress,
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light,
      final CallbackInfo ci) {
    final Arm arm =
        hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
    final FirstPersonSway sway = PlayerClips.sway(player, arm, tickDelta);
    if (sway.still()) {
      return;
    }
    final float side = arm == Arm.RIGHT ? 1.0F : -1.0F;
    matrices.translate(side * SHOULDER_X, SHOULDER_Y, SHOULDER_Z);
    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sway.pitchDegrees()));
    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sway.yawDegrees()));
    matrices.translate(-side * SHOULDER_X, -SHOULDER_Y, -SHOULDER_Z);
  }
}
