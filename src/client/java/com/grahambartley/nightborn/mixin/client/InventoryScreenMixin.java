package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.render.StatusMarks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
abstract class InventoryScreenMixin {

  @Inject(
      method =
          "drawEntity(Lnet/minecraft/client/gui/DrawContext;FFFLorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/entity/LivingEntity;)V",
      at = @At("HEAD"))
  private static void nightborn$portraitStart(
      final DrawContext context,
      final float x,
      final float y,
      final float size,
      final Vector3f offset,
      final Quaternionf rotation,
      final Quaternionf cameraRotation,
      final LivingEntity entity,
      final CallbackInfo ci) {
    StatusMarks.portrait(true);
  }

  @Inject(
      method =
          "drawEntity(Lnet/minecraft/client/gui/DrawContext;FFFLorg/joml/Vector3f;Lorg/joml/Quaternionf;Lorg/joml/Quaternionf;Lnet/minecraft/entity/LivingEntity;)V",
      at = @At("RETURN"))
  private static void nightborn$portraitEnd(
      final DrawContext context,
      final float x,
      final float y,
      final float size,
      final Vector3f offset,
      final Quaternionf rotation,
      final Quaternionf cameraRotation,
      final LivingEntity entity,
      final CallbackInfo ci) {
    StatusMarks.portrait(false);
  }
}
