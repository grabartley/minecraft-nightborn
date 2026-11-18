package com.grahambartley.nightborn.accessory;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.village.TradeOffer;

public final class GiftPrices {

  private GiftPrices() {}

  public static int cut(final int price, final double priceShare) {
    if (price <= 0 || priceShare >= 1.0) {
      return 0;
    }
    return (int) Math.round(price * (1.0 - Math.max(0.0, priceShare)));
  }

  public static void discount(
      final Iterable<TradeOffer> offers,
      final LivingEntity customer,
      final RegistryEntry<EntityAttribute> priceShare) {
    final double factor = GiftAttributes.value(customer, priceShare);
    for (final TradeOffer offer : offers) {
      offer.increaseSpecialPrice(-cut(offer.getOriginalFirstBuyItem().getCount(), factor));
    }
  }
}
