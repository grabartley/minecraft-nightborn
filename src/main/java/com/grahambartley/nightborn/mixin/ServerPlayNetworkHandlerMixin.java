package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.levitation.Levitation;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayNetworkHandler.class)
abstract class ServerPlayNetworkHandlerMixin {

  @Shadow public ServerPlayerEntity player;

  @ModifyExpressionValue(
      method = "onPlayerMove",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/server/network/ServerPlayerEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z"))
  private boolean nightborn$levitatingIsNotFloating(final boolean levitationEffect) {
    return levitationEffect || Levitation.isLevitating(player);
  }
}
