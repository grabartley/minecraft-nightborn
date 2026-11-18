package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.client.BatHangView;
import com.grahambartley.nightborn.client.moment.CeremonyCamera;
import com.grahambartley.nightborn.client.moment.CeremonyView;
import com.grahambartley.nightborn.coffin.CoffinSleepers;
import com.grahambartley.nightborn.render.CoffinSleeperView;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
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

  @Shadow
  protected abstract void setRotation(float yaw, float pitch);

  @Shadow
  protected abstract void setPos(double x, double y, double z);

  @Shadow
  protected abstract void moveBy(float forward, float up, float right);

  @Shadow
  protected abstract float clipToSpace(float desiredDistance);

  @Inject(method = "update", at = @At("TAIL"))
  private void nightborn$circleTheChosen(
      final BlockView area,
      final Entity focused,
      final boolean thirdPerson,
      final boolean inverseView,
      final float tickDelta,
      final CallbackInfo ci) {
    final CeremonyCamera.Shot shot = CeremonyView.shot(focused, tickDelta);
    if (shot == null) {
      return;
    }
    final Vec3d at = focused.getLerpedPos(tickDelta);
    setRotation(shot.yaw(), shot.pitch());
    setPos(at.x, at.y + focused.getStandingEyeHeight(), at.z);
    moveBy(-clipToSpace(shot.distance()), 0.0F, 0.0F);
  }

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
