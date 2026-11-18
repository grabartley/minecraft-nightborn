package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.batform.BatFormRules;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.levitation.LevitationRules;
import com.grahambartley.nightborn.moment.Moments;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
abstract class ClientPlayerEntityMixin {

  @Shadow public Input input;

  @Inject(
      method = "tickMovement",
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/client/input/Input;tick(ZF)V",
              shift = At.Shift.AFTER))
  private void nightborn$standStillInAMoment(final CallbackInfo info) {
    if (Moments.locked((ClientPlayerEntity) (Object) this)) {
      input.movementForward = 0.0F;
      input.movementSideways = 0.0F;
      input.pressingForward = false;
      input.pressingBack = false;
      input.pressingLeft = false;
      input.pressingRight = false;
      input.jumping = false;
      input.sneaking = false;
    }
  }

  @ModifyExpressionValue(
      method = "tickMovement",
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/client/network/ClientPlayerEntity;isOnGround()Z",
              ordinal = 0))
  private boolean nightborn$sprintStartsWhileLevitating(final boolean onGround) {
    return LevitationRules.movesAsGrounded(
        onGround, Levitation.isLevitating((ClientPlayerEntity) (Object) this));
  }

  @ModifyReturnValue(method = "isInSneakingPose", at = @At("RETURN"))
  private boolean nightborn$noCrouchWhileLevitating(final boolean crouching) {
    return LevitationRules.crouches(
        crouching, Levitation.holdsFloatPose((ClientPlayerEntity) (Object) this));
  }

  @ModifyExpressionValue(
      method = "tickMovement",
      at =
          @At(
              value = "FIELD",
              target = "Lnet/minecraft/entity/player/PlayerAbilities;allowFlying:Z"))
  private boolean nightborn$aBatHasNoCreativeFlight(final boolean allowFlying) {
    return BatFormRules.creativeFlight(
        allowFlying, BatForm.isBat((ClientPlayerEntity) (Object) this));
  }
}
