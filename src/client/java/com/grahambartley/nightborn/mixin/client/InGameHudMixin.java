package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.blood.BloodCapacity;
import com.grahambartley.nightborn.client.BloodBar;
import com.grahambartley.nightborn.client.accessory.DuskTint;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
abstract class InGameHudMixin {

  @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
  private void nightborn$drawBlood(
      final DrawContext context,
      final PlayerEntity player,
      final int top,
      final int right,
      final CallbackInfo ci) {
    if (Vampires.isVampire(player)) {
      BloodBar.draw(context, BloodAttachment.get(player), BloodCapacity.of(player), top, right);
      ci.cancel();
    }
  }

  @Inject(method = "renderMiscOverlays", at = @At("TAIL"))
  private void nightborn$duskThroughSmokedLenses(
      final DrawContext context, final RenderTickCounter tickCounter, final CallbackInfo ci) {
    DuskTint.draw(context);
  }
}
