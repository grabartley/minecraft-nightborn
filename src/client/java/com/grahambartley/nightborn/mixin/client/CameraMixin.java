package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.client.BatHangView;
import com.grahambartley.nightborn.coffin.CoffinSleepers;
import com.grahambartley.nightborn.render.CoffinSleeperView;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.BlockView;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
abstract class CameraMixin {

  @ModifyConstant(method = "update", constant = @Constant(floatValue = 0.3F))
  private float nightborn$lookAtTheLidFromInside(
      final float lift, @Local(argsOnly = true) final Entity focused) {
    return CoffinSleeperView.cameraLift(
        focused instanceof LivingEntity sleeper && CoffinSleepers.inCoffin(sleeper), lift);
  }

  @Shadow @Final private static Vector3f HORIZONTAL;
  @Shadow @Final private static Vector3f VERTICAL;
  @Shadow @Final private static Vector3f DIAGONAL;
  @Shadow @Final private Quaternionf rotation;
  @Shadow @Final private Vector3f horizontalPlane;
  @Shadow @Final private Vector3f verticalPlane;
  @Shadow @Final private Vector3f diagonalPlane;

  @Inject(method = "update", at = @At("TAIL"))
  private void nightborn$hangUpsideDown(
      final BlockView area,
      final Entity focused,
      final boolean thirdPerson,
      final boolean inverseView,
      final float tickDelta,
      final CallbackInfo ci) {
    final float roll = BatHangView.roll(thirdPerson, tickDelta);
    if (roll != 0.0F) {
      rotation.rotateZ(roll);
      HORIZONTAL.rotate(rotation, horizontalPlane);
      VERTICAL.rotate(rotation, verticalPlane);
      DIAGONAL.rotate(rotation, diagonalPlane);
    }
  }
}
