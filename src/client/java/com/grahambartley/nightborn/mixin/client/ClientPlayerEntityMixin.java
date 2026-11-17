package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.batform.BatFormRules;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.levitation.LevitationRules;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerEntity.class)
abstract class ClientPlayerEntityMixin {

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
