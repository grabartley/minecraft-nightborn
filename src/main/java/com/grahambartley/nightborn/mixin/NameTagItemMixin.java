package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.name.MobNaming;
import com.grahambartley.nightborn.name.NameBearer;
import com.grahambartley.nightborn.name.NameRules;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.NameTagItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NameTagItem.class)
abstract class NameTagItemMixin {

  @Inject(method = "useOnEntity", at = @At("HEAD"), cancellable = true)
  private void nightborn$nameBearer(
      final ItemStack stack,
      final PlayerEntity user,
      final LivingEntity entity,
      final Hand hand,
      final CallbackInfoReturnable<ActionResult> cir) {
    if (!(entity instanceof MobEntity mob) || !(entity instanceof NameBearer bearer)) {
      return;
    }
    final NameRules.Verdict verdict = NameRules.checkTag(stack.get(DataComponentTypes.CUSTOM_NAME));
    if (!verdict.ok() || !mob.isAlive()) {
      cir.setReturnValue(ActionResult.PASS);
      return;
    }
    if (!user.getWorld().isClient()) {
      bearer.bearEpithet("");
      MobNaming.give(mob, verdict.name().orElseThrow());
      mob.setPersistent();
      stack.decrement(1);
    }
    cir.setReturnValue(ActionResult.success(user.getWorld().isClient()));
  }
}
