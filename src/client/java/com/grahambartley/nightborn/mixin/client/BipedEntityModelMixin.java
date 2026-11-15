package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.client.anim.ClipLibrary;
import com.grahambartley.nightborn.client.anim.PlayerAnimator;
import com.grahambartley.nightborn.client.anim.PlayerPoser;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
abstract class BipedEntityModelMixin<T extends LivingEntity> {

  @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
  private void nightborn$playVampireClips(
      final T entity,
      final float limbAngle,
      final float limbDistance,
      final float animationProgress,
      final float headYaw,
      final float headPitch,
      final CallbackInfo ci) {
    if (entity instanceof PlayerEntity player) {
      PlayerPoser.pose(
          (BipedEntityModel<?>) (Object) this,
          PlayerAnimator.pose(player, animationProgress),
          ClipLibrary.current().rig());
    }
  }
}
