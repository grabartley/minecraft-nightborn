package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.coffin.CoffinSleepers;
import com.grahambartley.nightborn.coffin.RestRules;
import com.grahambartley.nightborn.core.VampireCore;
import com.grahambartley.nightborn.hunter.HunterAttributes;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.sickness.BiteSickness;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.vampire.VisibleStateAttachment;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
abstract class PlayerEntityMixin {

  @Inject(method = "createPlayerAttributes", at = @At("RETURN"))
  private static void nightborn$hunterAttributes(
      final CallbackInfoReturnable<DefaultAttributeContainer.Builder> cir) {
    HunterAttributes.addTo(cir.getReturnValue());
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
      VampireCore.refreshBareHands(self, ServerConfigService.current().core());
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

  @Inject(method = "eatFood", at = @At("HEAD"))
  private void nightborn$cureWithFood(
      final World world,
      final ItemStack stack,
      final FoodComponent food,
      final CallbackInfoReturnable<ItemStack> cir) {
    if ((Object) this instanceof ServerPlayerEntity player) {
      BiteSickness.onEaten(player, stack);
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
    return RestRules.wrongTime(CoffinSleepers.inCoffin((PlayerEntity) (Object) this), day);
  }
}
