package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.vampire.Vampires;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.spawner.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PhantomSpawner.class)
abstract class PhantomSpawnerMixin {

  @WrapOperation(
      method = "spawn",
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
  private boolean nightborn$skipVampires(
      final ServerPlayerEntity player, final Operation<Boolean> original) {
    return original.call(player) || Vampires.isVampire(player);
  }
}
