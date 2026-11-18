package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.advancement.AdvancementAudience;
import com.grahambartley.nightborn.journal.JournalPages;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerAdvancementTracker.class)
abstract class PlayerAdvancementTrackerMixin {

  @Shadow private ServerPlayerEntity owner;

  @Shadow @Final private PlayerManager playerManager;

  @Inject(method = "method_53637", at = @At("HEAD"))
  private void nightborn$turnTheJournalPages(
      final AdvancementEntry advancement, final AdvancementDisplay display, final CallbackInfo ci) {
    JournalPages.later(this.owner);
  }

  @Redirect(
      method = "method_53637",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"))
  private void nightborn$keepAVampiresSecretFromHumans(
      final PlayerManager players,
      final Text message,
      final boolean overlay,
      final AdvancementEntry advancement,
      final AdvancementDisplay display) {
    AdvancementAudience.announce(players, this.owner, advancement.id(), message);
  }
}
