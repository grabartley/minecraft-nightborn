package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.coffin.CoffinSleepers;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.levitation.LevitationMotion;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.VampireTicker;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.vomit.Consumables;
import com.grahambartley.nightborn.vomit.Vomiting;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {

  @Inject(method = "tick", at = @At("TAIL"))
  private void nightborn$tickVampire(final CallbackInfo ci) {
    final LivingEntity self = (LivingEntity) (Object) this;
    if (!self.getWorld().isClient && Vampires.isVampire(self)) {
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

  @Inject(method = "travel", at = @At("HEAD"))
  private void nightborn$hoverWhileLevitating(final Vec3d movementInput, final CallbackInfo ci) {
    final LivingEntity self = (LivingEntity) (Object) this;
    if (LevitationMotion.drives(self)) {
      LevitationMotion.hover(self);
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
      return Vomiting.swallow(player, stack, ServerConfigService.current().blood());
    }
    return original.call(stack, world, user);
  }

  @ModifyConstant(method = "setPositionInBed", constant = @Constant(doubleValue = 0.6875))
  private double nightborn$lieInsideTheCoffin(
      final double height, @Local(argsOnly = true) final BlockPos pos) {
    return CoffinSleepers.isCoffin(((LivingEntity) (Object) this).getWorld(), pos)
        ? CoffinSleepers.LYING_HEIGHT
        : height;
  }
}
