package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.client.boss.BossArenaView;
import com.grahambartley.nightborn.client.gramophone.GramophoneMusic;
import com.grahambartley.nightborn.sound.BossMusicEnd;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MusicTracker;
import net.minecraft.client.sound.SoundInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicTracker.class)
abstract class MusicTrackerMixin {

  @Shadow @Final private MinecraftClient client;
  @Shadow private SoundInstance current;

  @Shadow
  public abstract void stop();

  @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
  private void nightborn$yieldToGramophone(final CallbackInfo info) {
    if (!GramophoneMusic.playing(client)) {
      return;
    }
    if (current != null) {
      stop();
    }
    info.cancel();
  }

  @Inject(method = "tick", at = @At("HEAD"))
  private void nightborn$endBossMusicOutsideTheArena(final CallbackInfo info) {
    if (current != null
        && BossMusicEnd.stops(current.getId(), BossArenaView.bossTracks(), BossArenaView.track())) {
      stop();
    }
  }
}
