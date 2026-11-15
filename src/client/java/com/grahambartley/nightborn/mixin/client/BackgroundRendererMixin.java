package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.client.fog.StructureFog;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
abstract class BackgroundRendererMixin {

  @Inject(method = "applyFog", at = @At("TAIL"))
  private static void nightborn$thickenNearVampireStructures(
      final Camera camera,
      final BackgroundRenderer.FogType fogType,
      final float viewDistance,
      final boolean thickFog,
      final float tickDelta,
      final CallbackInfo info) {
    if (camera.getSubmersionType() == CameraSubmersionType.NONE) {
      StructureFog.apply();
    }
  }
}
