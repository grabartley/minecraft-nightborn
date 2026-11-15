package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.client.anim.ClipLibrary;
import com.grahambartley.nightborn.client.anim.PlayerAnimator;
import com.grahambartley.nightborn.client.anim.PlayerPoser;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
abstract class PlayerEntityRendererMixin {

  @Inject(method = "setupTransforms", at = @At("TAIL"))
  private void nightborn$tiltTheWholeBody(
      final AbstractClientPlayerEntity player,
      final MatrixStack matrices,
      final float animationProgress,
      final float bodyYaw,
      final float tickDelta,
      final float scale,
      final CallbackInfo ci) {
    PlayerPoser.tilt(
        matrices, PlayerAnimator.pose(player, animationProgress), ClipLibrary.current().rig());
  }

  @Inject(method = "renderArm", at = @At("HEAD"))
  private void nightborn$firstPersonArmStaysVanilla(final CallbackInfo ci) {
    PlayerAnimator.drawingFirstPersonArm(true);
  }

  @Inject(method = "renderArm", at = @At("RETURN"))
  private void nightborn$firstPersonArmDone(final CallbackInfo ci) {
    PlayerAnimator.drawingFirstPersonArm(false);
  }
}
