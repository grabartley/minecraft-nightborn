package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.levitation.LevitationRules;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
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
}
