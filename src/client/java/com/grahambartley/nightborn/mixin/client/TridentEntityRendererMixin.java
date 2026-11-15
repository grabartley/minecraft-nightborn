package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.render.CoatingLayers;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.TridentEntityRenderer;
import net.minecraft.entity.projectile.TridentEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TridentEntityRenderer.class)
abstract class TridentEntityRendererMixin {

  @WrapOperation(
      method =
          "render(Lnet/minecraft/entity/projectile/TridentEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/render/item/ItemRenderer;getDirectItemGlintConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/RenderLayer;ZZ)Lnet/minecraft/client/render/VertexConsumer;"))
  private VertexConsumer nightborn$coatingSheen(
      final VertexConsumerProvider provider,
      final RenderLayer layer,
      final boolean solid,
      final boolean glint,
      final Operation<VertexConsumer> original,
      @Local(argsOnly = true) final TridentEntity trident) {
    return CoatingLayers.entity(
        original.call(provider, layer, solid, glint), trident.getItemStack(), provider);
  }
}
