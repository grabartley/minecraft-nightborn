package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.faction.FactionPlayerList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin {

  @Inject(method = "getPlayerListName", at = @At("HEAD"), cancellable = true)
  private void nightborn$showFaction(final CallbackInfoReturnable<Text> cir) {
    if (FactionPlayerList.shown()) {
      cir.setReturnValue(FactionPlayerList.listName((ServerPlayerEntity) (Object) this, true));
    }
  }
}
