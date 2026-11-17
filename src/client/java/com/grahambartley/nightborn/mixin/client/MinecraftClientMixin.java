package com.grahambartley.nightborn.mixin.client;

import com.grahambartley.nightborn.blood.PreySense;
import com.grahambartley.nightborn.client.boss.BossArenaView;
import com.grahambartley.nightborn.client.structure.NearbyStructure;
import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.sound.GothicMusic;
import com.grahambartley.nightborn.vampire.VampireAttributes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.MusicSound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
abstract class MinecraftClientMixin {

  @Shadow public ClientWorld world;

  @Shadow public ClientPlayerEntity player;

  @Inject(method = "hasOutline", at = @At("HEAD"), cancellable = true)
  private void nightborn$sensePrey(
      final Entity entity, final CallbackInfoReturnable<Boolean> info) {
    if (player != null
        && PreySense.reveals(
            VampireAttributes.value(player, VampireAttributes.PREY_SENSE),
            player.squaredDistanceTo(entity),
            entity instanceof LivingEntity,
            entity == player)) {
      info.setReturnValue(true);
    }
  }

  @Inject(method = "getMusicType", at = @At("RETURN"), cancellable = true)
  private void nightborn$playGothicMusic(final CallbackInfoReturnable<MusicSound> info) {
    final boolean night =
        world != null
            && NightTime.isNight(world.getDimension().hasFixedTime(), world.getTimeOfDay());
    info.setReturnValue(
        GothicMusic.choose(
            info.getReturnValue(), BossArenaView.music(), NearbyStructure.music(), night));
  }
}
