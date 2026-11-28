package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.guild.GuildSeller;
import net.minecraft.entity.Entity;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.village.Merchant;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreenHandler.class)
abstract class MerchantScreenHandlerMixin {

  @Shadow @Final private Merchant merchant;

  @Inject(method = "playYesSound", at = @At("HEAD"), cancellable = true)
  private void nightborn$aGuildSellerSoundsFromItsOwnBody(final CallbackInfo ci) {
    if (!(merchant instanceof GuildSeller counter)) {
      return;
    }
    if (!merchant.isClient()) {
      final Entity seller = counter.seller();
      seller
          .getWorld()
          .playSound(
              null,
              seller.getX(),
              seller.getY(),
              seller.getZ(),
              merchant.getYesSound(),
              SoundCategory.NEUTRAL,
              1.0F,
              1.0F);
    }
    ci.cancel();
  }
}
