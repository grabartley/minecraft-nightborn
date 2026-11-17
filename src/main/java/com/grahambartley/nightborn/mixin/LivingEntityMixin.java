package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.batform.BatFlightMotion;
import com.grahambartley.nightborn.coffin.CoffinLayout;
import com.grahambartley.nightborn.coffin.CoffinRise;
import com.grahambartley.nightborn.coffin.CoffinSleeperHold;
import com.grahambartley.nightborn.coffin.CoffinSleepers;
import com.grahambartley.nightborn.downed.DownedTargeting;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.levitation.LevitationMotion;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.VampireTicker;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.vanish.Vanish;
import com.grahambartley.nightborn.vomit.Consumables;
import com.grahambartley.nightborn.vomit.Vomiting;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Optional;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {

  @Shadow @Final private static TrackedData<Optional<BlockPos>> SLEEPING_POSITION;

  @Shadow protected boolean jumping;

  @Inject(method = "tick", at = @At("TAIL"))
  private void nightborn$tickVampire(final CallbackInfo ci) {
    final LivingEntity self = (LivingEntity) (Object) this;
    if (self.getWorld().isClient) {
      return;
    }
    Vanish.tick(self);
    if (Vampires.isVampire(self)) {
      VampireTicker.tick(self);
    }
  }

  @Inject(
      method = "canTarget(Lnet/minecraft/entity/LivingEntity;)Z",
      at = @At("HEAD"),
      cancellable = true)
  private void nightborn$phantomsIgnoreVampires(
      final LivingEntity target, final CallbackInfoReturnable<Boolean> cir) {
    if ((Object) this instanceof PhantomEntity && Vampires.isVampire(target)) {
      cir.setReturnValue(false);
    }
  }

  @Inject(
      method = "canTarget(Lnet/minecraft/entity/LivingEntity;)Z",
      at = @At("HEAD"),
      cancellable = true)
  private void nightborn$onlyHuntersTargetTheDowned(
      final LivingEntity target, final CallbackInfoReturnable<Boolean> cir) {
    if (DownedTargeting.spares((LivingEntity) (Object) this, target)) {
      cir.setReturnValue(false);
    }
  }

  @Inject(method = "travel", at = @At("HEAD"))
  private void nightborn$hoverWhileLevitating(final Vec3d movementInput, final CallbackInfo ci) {
    final LivingEntity self = (LivingEntity) (Object) this;
    if (LevitationMotion.drives(self)) {
      LevitationMotion.hover(self, movementInput);
    }
  }

  @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
  private void nightborn$flyAsABat(final Vec3d movementInput, final CallbackInfo ci) {
    final LivingEntity self = (LivingEntity) (Object) this;
    if (BatFlightMotion.drives(self)) {
      BatFlightMotion.travel(self, movementInput, jumping);
      ci.cancel();
    }
  }

  @Inject(method = "getMovementSpeed(F)F", at = @At("HEAD"), cancellable = true)
  private void nightborn$walkingPaceWhileLevitating(
      final float slipperiness, final CallbackInfoReturnable<Float> cir) {
    final LivingEntity self = (LivingEntity) (Object) this;
    if (!self.isOnGround() && Levitation.isLevitating(self)) {
      cir.setReturnValue(LevitationMotion.airSpeed(self));
    }
  }

  @ModifyVariable(method = "updateLimbs(F)V", at = @At("HEAD"), argsOnly = true)
  private float nightborn$feetStillWhileLevitating(final float posDelta) {
    return Levitation.isLevitating((LivingEntity) (Object) this) ? 0.0F : posDelta;
  }

  @WrapOperation(
      method = "consumeItem",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/item/ItemStack;finishUsing(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;"))
  private ItemStack nightborn$vampiresKeepNothingDown(
      final ItemStack stack,
      final World world,
      final LivingEntity user,
      final Operation<ItemStack> original) {
    if (user instanceof PlayerEntity player
        && Consumables.rejectedBy(stack, Vampires.isVampire(player))) {
      return Vomiting.swallow(player, stack, ServerConfigService.current());
    }
    return original.call(stack, world, user);
  }

  @Inject(method = "setPositionInBed", at = @At("HEAD"), cancellable = true)
  private void nightborn$restInsideTheCoffin(final BlockPos pos, final CallbackInfo ci) {
    final LivingEntity self = (LivingEntity) (Object) this;
    CoffinSleepers.restingSpot(self.getWorld(), pos)
        .ifPresent(
            spot -> {
              self.setPosition(spot);
              ci.cancel();
            });
  }

  @Inject(method = "sleep", at = @At("TAIL"))
  private void nightborn$standInAStandingCoffin(final BlockPos pos, final CallbackInfo ci) {
    CoffinSleeperHold.hold((LivingEntity) (Object) this);
  }

  @Inject(method = "tick", at = @At("TAIL"))
  private void nightborn$holdTheSleeperInsideTheCoffin(final CallbackInfo ci) {
    CoffinSleeperHold.hold((LivingEntity) (Object) this);
  }

  @Inject(method = "tick", at = @At("TAIL"))
  private void nightborn$finishRisingFromTheCoffin(final CallbackInfo ci) {
    CoffinRise.tick((LivingEntity) (Object) this);
  }

  @Inject(method = "wakeUp", at = @At("HEAD"), cancellable = true)
  private void nightborn$riseBeforeAVampireMobWakes(final CallbackInfo ci) {
    final LivingEntity self = (LivingEntity) (Object) this;
    if (!(self instanceof PlayerEntity) && CoffinRise.deferWakeIntoRise(self)) {
      ci.cancel();
    }
  }

  @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
  private void nightborn$fitTheUprightSleeperInsideTheCoffin(
      final EntityPose pose, final CallbackInfoReturnable<EntityDimensions> cir) {
    if (pose == EntityPose.STANDING && CoffinSleepers.upright((LivingEntity) (Object) this)) {
      cir.setReturnValue(CoffinLayout.uprightSleeper(cir.getReturnValue()));
    }
  }

  @Inject(method = "onTrackedDataSet", at = @At("TAIL"))
  private void nightborn$refitWhenTheSleeperComesOrGoes(
      final TrackedData<?> data, final CallbackInfo ci) {
    if (SLEEPING_POSITION.equals(data)) {
      ((LivingEntity) (Object) this).calculateDimensions();
    }
  }
}
