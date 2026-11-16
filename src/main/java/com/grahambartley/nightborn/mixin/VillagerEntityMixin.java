package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.mind.Charm;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
abstract class VillagerEntityMixin {

  @Inject(method = "prepareOffersFor", at = @At("TAIL"))
  private void nightborn$charmedPrices(final PlayerEntity player, final CallbackInfo ci) {
    Charm.discount((VillagerEntity) (Object) this, player);
  }
}
