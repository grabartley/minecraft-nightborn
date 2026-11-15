package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.coffin.CoffinSleepers;
import com.grahambartley.nightborn.coffin.RestRules;
import com.grahambartley.nightborn.faction.FactionPlayerList;
import com.grahambartley.nightborn.vampire.Vampires;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import net.minecraft.entity.player.PlayerEntity.SleepFailureReason;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin {

  @Inject(method = "getPlayerListName", at = @At("HEAD"), cancellable = true)
  private void nightborn$showFaction(final CallbackInfoReturnable<Text> cir) {
    if (FactionPlayerList.shown()) {
      cir.setReturnValue(FactionPlayerList.listName((ServerPlayerEntity) (Object) this, true));
    }
  }

  @Inject(method = "trySleep", at = @At("HEAD"), cancellable = true)
  private void nightborn$restOnlyWhereYourKindMay(
      final BlockPos pos, final CallbackInfoReturnable<Either<SleepFailureReason, Unit>> cir) {
    final ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
    RestRules.refusal(Vampires.isVampire(self), CoffinSleepers.isCoffin(self.getWorld(), pos))
        .ifPresent(
            key -> {
              self.sendMessage(Text.translatable(key), true);
              cir.setReturnValue(Either.left(SleepFailureReason.OTHER_PROBLEM));
            });
  }

  @ModifyExpressionValue(
      method = "trySleep",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isDay()Z"))
  private boolean nightborn$coffinsAreForTheDay(
      final boolean day, @Local(argsOnly = true) final BlockPos pos) {
    final ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
    return RestRules.wrongTime(
        CoffinSleepers.isCoffin(self.getWorld(), pos),
        day,
        self.getWorld().getDimension().hasFixedTime(),
        self.getWorld().getTimeOfDay());
  }
}
