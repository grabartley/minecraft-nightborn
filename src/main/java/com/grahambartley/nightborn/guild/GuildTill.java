package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.grahambartley.nightborn.accessory.GiftPrices;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOffer;

public final class GuildTill {

  private GuildTill() {}

  public static void open(
      final Merchant counter,
      final ServerPlayerEntity visitor,
      final Text name,
      final Text nothing) {
    if (counter.getOffers().isEmpty()) {
      visitor.sendMessage(nothing, true);
      return;
    }
    counter.getOffers().forEach(TradeOffer::clearSpecialPrice);
    GiftPrices.discount(counter.getOffers(), visitor, GiftAttributes.GUILD_PRICE);
    counter.setCustomer(visitor);
    counter.sendOffers(visitor, name, 1);
  }
}
