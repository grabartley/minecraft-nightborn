package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.coffin.CoffinBlock;
import com.grahambartley.nightborn.coffin.CoffinOwnership;
import com.grahambartley.nightborn.coffin.CoffinRise;
import com.grahambartley.nightborn.coffin.CoffinSleepers;
import com.grahambartley.nightborn.coffin.RestRules;
import com.grahambartley.nightborn.faction.FactionPlayerList;
import com.grahambartley.nightborn.vampire.Vampires;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import java.util.Set;
import net.minecraft.entity.player.PlayerEntity.SleepFailureReason;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin {

  @Inject(method = "getPlayerListName", at = @At("HEAD"), cancellable = true)
  private void nightborn$showFaction(final CallbackInfoReturnable<Text> cir) {
    if (FactionPlayerList.shown()) {
      cir.setReturnValue(FactionPlayerList.listName((ServerPlayerEntity) (Object) this, true));
    }
  }

  @Inject(method = "wakeUp", at = @At("HEAD"), cancellable = true)
  private void nightborn$riseFromTheCoffinBeforeWaking(
      final boolean skipSleepTimer, final boolean updateSleepingPlayers, final CallbackInfo ci) {
    if (CoffinRise.deferWakeIntoRise((ServerPlayerEntity) (Object) this)) {
      ci.cancel();
    }
  }

  @Inject(
      method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z",
      at = @At("HEAD"))
  private void nightborn$wakeAtOnceBeforeATeleport(
      final ServerWorld world,
      final double x,
      final double y,
      final double z,
      final Set<PositionFlag> flags,
      final float yaw,
      final float pitch,
      final CallbackInfoReturnable<Boolean> cir) {
    CoffinRise.wakeAtOnce((ServerPlayerEntity) (Object) this);
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

  @WrapWithCondition(
      method = "trySleep",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/server/network/ServerPlayerEntity;setSpawnPoint(Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/util/math/BlockPos;FZZ)V"))
  private boolean nightborn$onlyYourOwnCoffinBecomesYourRespawn(
      final ServerPlayerEntity self,
      final RegistryKey<World> dimension,
      final BlockPos pos,
      final float angle,
      final boolean forced,
      final boolean sendMessage) {
    return CoffinOwnership.rest(self, pos);
  }

  @Inject(method = "isBedObstructed", at = @At("HEAD"), cancellable = true)
  private void nightborn$aStandingCoffinNeedsRoomInFront(
      final BlockPos pos, final Direction direction, final CallbackInfoReturnable<Boolean> cir) {
    final ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
    if (CoffinBlock.isStanding(self.getWorld().getBlockState(pos))) {
      cir.setReturnValue(CoffinSleepers.blockedInFront(self.getWorld(), pos));
    }
  }
}
