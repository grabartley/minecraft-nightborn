package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.batform.BatShape;
import com.grahambartley.nightborn.clothing.WornAttributes;
import com.grahambartley.nightborn.coffin.CoffinSleepers;
import com.grahambartley.nightborn.coffin.RestRules;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.core.VampireCore;
import com.grahambartley.nightborn.hunter.HunterAttributes;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.mind.MindAttributes;
import com.grahambartley.nightborn.occult.MistForm;
import com.grahambartley.nightborn.occult.OccultAttributes;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.sickness.BiteSickness;
import com.grahambartley.nightborn.vampire.VampireAttributes;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.vampire.VisibleStateAttachment;
import com.grahambartley.nightborn.vomit.Meals;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
abstract class PlayerEntityMixin {

  @Unique private final BatShape nightborn$batShape = new BatShape();

  @Unique private final BatShape nightborn$mistShape = new BatShape();

  @Inject(method = "createPlayerAttributes", at = @At("RETURN"))
  private static void nightborn$hunterAttributes(
      final CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
    HunterAttributes.addTo(cir.getReturnValue());
    MindAttributes.addTo(cir.getReturnValue());
    OccultAttributes.addTo(cir.getReturnValue());
    VampireAttributes.addTo(cir.getReturnValue());
    WornAttributes.addTo(cir.getReturnValue());
    GiftAttributes.addTo(cir.getReturnValue());
  }

  @Inject(method = "canConsume", at = @At("HEAD"), cancellable = true)
  private void nightborn$vampiresMayAlwaysEat(
      final boolean ignoreHunger, final CallbackInfoReturnable<Boolean> cir) {
    if (Vampires.isVampire((PlayerEntity) (Object) this)) {
      cir.setReturnValue(true);
    }
  }

  @Inject(method = "attack", at = @At("HEAD"))
  private void nightborn$bareHandsMatchTheBlow(final Entity target, final CallbackInfo ci) {
    final PlayerEntity self = (PlayerEntity) (Object) this;
    if (!self.getWorld().isClient && Vampires.isVampire(self)) {
      final NightbornConfig config = ServerConfigService.current();
      VampireCore.refreshBareHands(self, config.core(), config.blood());
    }
  }

  @Inject(method = "updatePose", at = @At("HEAD"), cancellable = true)
  private void nightborn$collapseWhenDowned(final CallbackInfo ci) {
    final PlayerEntity self = (PlayerEntity) (Object) this;
    if (VisibleStateAttachment.get(self).downed()) {
      self.setPose(EntityPose.SWIMMING);
      ci.cancel();
    }
  }

  @Inject(method = "updatePose", at = @At("HEAD"), cancellable = true)
  private void nightborn$sleepStandingInAStandingCoffin(final CallbackInfo ci) {
    final PlayerEntity self = (PlayerEntity) (Object) this;
    if (CoffinSleepers.upright(self)) {
      self.setPose(EntityPose.STANDING);
      ci.cancel();
    }
  }

  @Inject(method = "updatePose", at = @At("HEAD"), cancellable = true)
  private void nightborn$keepTheFloatPoseWhileLevitating(final CallbackInfo ci) {
    final PlayerEntity self = (PlayerEntity) (Object) this;
    if (Levitation.holdsFloatPose(self)) {
      self.setPose(EntityPose.STANDING);
      ci.cancel();
    }
  }

  @Inject(method = "eatFood", at = @At("HEAD"))
  private void nightborn$cureWithFood(
      final World world,
      final ItemStack stack,
      final FoodComponent food,
      final CallbackInfoReturnable<ItemStack> cir) {
    if ((Object) this instanceof ServerPlayerEntity player) {
      BiteSickness.onEaten(player, stack);
      Meals.eaten(player, stack);
    }
  }

  @WrapWithCondition(
      method = "tick",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/entity/player/PlayerEntity;incrementStat(Lnet/minecraft/util/Identifier;)V"))
  private boolean nightborn$vampiresNeverTire(final PlayerEntity player, final Identifier stat) {
    return stat != Stats.TIME_SINCE_REST || !Vampires.isVampire(player);
  }

  @ModifyExpressionValue(
      method = "tick",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isDay()Z"))
  private boolean nightborn$coffinSleepersRiseAtDusk(final boolean day) {
    final PlayerEntity self = (PlayerEntity) (Object) this;
    return RestRules.wrongTime(
        CoffinSleepers.inCoffin(self),
        day,
        self.getWorld().getDimension().hasFixedTime(),
        self.getWorld().getTimeOfDay());
  }

  @Inject(method = "getBaseDimensions", at = @At("HEAD"), cancellable = true)
  private void nightborn$batSized(
      final EntityPose pose, final CallbackInfoReturnable<EntityDimensions> cir) {
    if (BatForm.isBat((PlayerEntity) (Object) this)) {
      cir.setReturnValue(BatForm.dimensions((PlayerEntity) (Object) this));
    } else if (MistForm.on((PlayerEntity) (Object) this)) {
      cir.setReturnValue(MistForm.DIMENSIONS);
    }
  }

  @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
  private void nightborn$aBatNeverTakesFallDamage(
      final float fallDistance,
      final float damageMultiplier,
      final DamageSource damageSource,
      final CallbackInfoReturnable<Boolean> cir) {
    if (BatForm.isBat((PlayerEntity) (Object) this)) {
      cir.setReturnValue(false);
    }
  }

  @Inject(method = "tick", at = @At("HEAD"))
  private void nightborn$reshapeOnBatForm(final CallbackInfo ci) {
    final PlayerEntity self = (PlayerEntity) (Object) this;
    if (nightborn$batShape.changed(BatForm.isBat(self))
        | nightborn$mistShape.changed(MistForm.on(self))) {
      self.calculateDimensions();
    }
  }
}
