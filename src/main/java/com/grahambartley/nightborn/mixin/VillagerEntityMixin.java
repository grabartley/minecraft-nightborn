package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.grahambartley.nightborn.accessory.GiftPrices;
import com.grahambartley.nightborn.mind.Charm;
import com.grahambartley.nightborn.village.NightMarkup;
import com.grahambartley.nightborn.village.VillageSuspicion;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
abstract class VillagerEntityMixin {

  @Inject(method = "prepareOffersFor", at = @At("TAIL"))
  private void nightborn$charmedPrices(final PlayerEntity player, final CallbackInfo ci) {
    final VillagerEntity villager = (VillagerEntity) (Object) this;
    Charm.discount(villager, player);
    GiftPrices.discount(villager.getOffers(), player, GiftAttributes.VILLAGER_PRICE);
    NightMarkup.charge(villager, player);
  }

  @Inject(method = "loot", at = @At("HEAD"))
  private void nightborn$gift(final ItemEntity item, final CallbackInfo ci) {
    final VillagerEntity villager = (VillagerEntity) (Object) this;
    if (villager.getWorld() instanceof ServerWorld world
        && item.getOwner() instanceof PlayerEntity giver) {
      VillageSuspicion.gift(world, villager, giver);
    }
  }

  @Inject(method = "afterUsing", at = @At("HEAD"))
  private void nightborn$fairTrade(final TradeOffer offer, final CallbackInfo ci) {
    final VillagerEntity villager = (VillagerEntity) (Object) this;
    if (villager.getWorld() instanceof ServerWorld world && villager.getCustomer() != null) {
      VillageSuspicion.gift(world, villager, villager.getCustomer());
    }
  }
}
