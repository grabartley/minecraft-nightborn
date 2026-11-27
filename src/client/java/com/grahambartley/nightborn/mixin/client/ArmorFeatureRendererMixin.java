package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.dismember.Dismemberment;
import com.grahambartley.nightborn.render.LostLimbs;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
abstract class ArmorFeatureRendererMixin {

  @Inject(
      method = "renderArmor",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;setVisible(Lnet/minecraft/client/render/entity/model/BipedEntityModel;Lnet/minecraft/entity/EquipmentSlot;)V",
              shift = At.Shift.AFTER))
  private void nightborn$hideLostLimbs(
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final LivingEntity entity,
      final EquipmentSlot slot,
      final int light,
      final BipedEntityModel<?> model,
      final CallbackInfo ci) {
    LostLimbs.hide(model, Dismemberment.state(entity));
  }
}
