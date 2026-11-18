package com.grahambartley.nightborn.village;

import com.grahambartley.nightborn.core.NightTime;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.TradeOffer;

public final class NightMarkup {

  private NightMarkup() {}

  public static boolean applies(final VillageStep step, final boolean night, final boolean hero) {
    return night && step.atLeast(VillageStep.WARY) && !hero;
  }

  public static int added(final int price, final int percent) {
    return price <= 0 || percent <= 0 ? 0 : Math.max((price * percent + 99) / 100, 1);
  }

  public static void charge(final VillagerEntity villager, final PlayerEntity customer) {
    if (!(villager.getWorld() instanceof ServerWorld world)) {
      return;
    }
    final boolean known =
        VillageSuspicion.bellOf(villager)
            .filter(
                bell ->
                    applies(
                        VillageSuspicion.step(world, bell),
                        NightTime.isNight(
                            world.getDimension().hasFixedTime(), world.getTimeOfDay()),
                        customer.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE)))
            .isPresent();
    if (!known) {
      return;
    }
    final int percent = VillageSuspicion.settings().nightMarkup();
    for (final TradeOffer offer : villager.getOffers()) {
      offer.increaseSpecialPrice(added(offer.getOriginalFirstBuyItem().getCount(), percent));
    }
  }
}
