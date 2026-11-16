package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.client.structure.NearbyStructure;
import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.sound.GothicMusic;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.MusicSound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
abstract class MinecraftClientMixin {

  @Shadow public ClientWorld world;

  @Inject(method = "getMusicType", at = @At("RETURN"), cancellable = true)
  private void nightborn$playGothicMusic(final CallbackInfoReturnable<MusicSound> info) {
    final boolean night =
        world != null
            && NightTime.isNight(world.getDimension().hasFixedTime(), world.getTimeOfDay());
    info.setReturnValue(GothicMusic.choose(info.getReturnValue(), NearbyStructure.music(), night));
  }
}
