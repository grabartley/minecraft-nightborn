package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.vampire.Vampires;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.SleepManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SleepManager.class)
abstract class SleepManagerMixin {

  @ModifyVariable(
      method = {"update", "canResetTime"},
      at = @At("HEAD"),
      argsOnly = true)
  private List<ServerPlayerEntity> nightborn$onlyHumansShareTheBeds(
      final List<ServerPlayerEntity> players) {
    return players.stream().filter(player -> !Vampires.isVampire(player)).toList();
  }
}
