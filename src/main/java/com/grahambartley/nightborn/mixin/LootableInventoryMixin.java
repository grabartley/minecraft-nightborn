package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.LootableInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LootableInventory.class)
interface LootableInventoryMixin {

  @ModifyExpressionValue(
      method = "generateLoot",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getLuck()F"))
  private float nightborn$chestLuck(
      final float luck, @Local(argsOnly = true) final PlayerEntity player) {
    return luck + (float) GiftAttributes.value(player, GiftAttributes.CHEST_LUCK);
  }
}
