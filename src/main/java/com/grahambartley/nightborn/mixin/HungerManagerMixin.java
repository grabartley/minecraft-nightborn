package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.blood.BloodTicker;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
abstract class HungerManagerMixin {

  @Inject(method = "update", at = @At("HEAD"), cancellable = true)
  private void nightborn$liveOnBlood(final PlayerEntity player, final CallbackInfo ci) {
    if (Vampires.isVampire(player)) {
      BloodTicker.tick(player, (HungerManager) (Object) this);
      ci.cancel();
    }
  }
}
