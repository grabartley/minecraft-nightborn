package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.render.CoatingLayers;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
abstract class ItemRendererMixin {

  @ModifyVariable(
      method =
          "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
      at = @At("STORE"))
  private VertexConsumer nightborn$coatingSheen(
      final VertexConsumer consumer,
      @Local(argsOnly = true) final ItemStack stack,
      @Local(argsOnly = true) final VertexConsumerProvider provider,
      @Local final RenderLayer layer) {
    return CoatingLayers.item(consumer, stack, provider, layer);
  }
}
