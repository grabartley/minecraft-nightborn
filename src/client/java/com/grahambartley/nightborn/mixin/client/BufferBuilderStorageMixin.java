package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.render.SheenLayers;
import java.util.SequencedMap;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.BufferAllocator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BufferBuilderStorage.class)
abstract class BufferBuilderStorageMixin {

  @ModifyArg(
      method = "<init>",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/render/VertexConsumerProvider;immediate(Ljava/util/SequencedMap;Lnet/minecraft/client/util/BufferAllocator;)Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;"),
      index = 0)
  private SequencedMap<RenderLayer, BufferAllocator> nightborn$coatingBuffers(
      final SequencedMap<RenderLayer, BufferAllocator> buffers) {
    SheenLayers.all()
        .forEach(layer -> buffers.put(layer, new BufferAllocator(layer.getExpectedBufferSize())));
    return buffers;
  }
}
