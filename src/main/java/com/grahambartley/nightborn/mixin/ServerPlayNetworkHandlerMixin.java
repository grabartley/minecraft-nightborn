package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.batform.BatHang;
import com.grahambartley.nightborn.levitation.Levitation;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
  private boolean nightborn$hoveringIsNotFloating(final boolean levitationEffect) {
    return levitationEffect || Levitation.isLevitating(player) || BatForm.isBat(player);
  }

  @Inject(method = "onPlayerMove", at = @At("RETURN"))
  private void nightborn$sneakLandsALevitatingPlayer(
      final PlayerMoveC2SPacket packet, final CallbackInfo ci) {
    Levitation.touchDown(player, packet.isOnGround());
  }

  @Inject(method = "onPlayerMove", at = @At("RETURN"))
  private void nightborn$aBatMovedOffItsSpotLetsGo(
      final PlayerMoveC2SPacket packet, final CallbackInfo ci) {
    BatHang.afterMove(player);
  }
}
