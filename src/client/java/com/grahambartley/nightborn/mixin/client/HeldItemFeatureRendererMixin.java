package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.dismember.BodyPart;
import com.grahambartley.nightborn.dismember.Dismemberment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemFeatureRenderer.class)
abstract class HeldItemFeatureRendererMixin {

  @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
  private void nightborn$dropFromLostHand(
      final LivingEntity entity,
      final ItemStack stack,
      final ModelTransformationMode mode,
      final Arm arm,
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light,
      final CallbackInfo ci) {
    if (Dismemberment.missing(entity, BodyPart.arm(arm))) {
      ci.cancel();
    }
  }
}
