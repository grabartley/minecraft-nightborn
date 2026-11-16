package com.grahambartley.nightborn.guild;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;

public record GuildOffer(
    int order, TradedItem wants, Optional<TradedItem> alsoWants, ItemStack gives, int maxUses) {

  public static final int MOST_USES = 64;

  public static final Codec<GuildOffer> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.INT.optionalFieldOf("order", 0).forGetter(GuildOffer::order),
                      TradedItem.CODEC.fieldOf("wants").forGetter(GuildOffer::wants),
                      TradedItem.CODEC
                          .optionalFieldOf("also_wants")
                          .forGetter(GuildOffer::alsoWants),
                      ItemStack.CODEC.fieldOf("gives").forGetter(GuildOffer::gives),
                      Codec.intRange(1, MOST_USES)
                          .optionalFieldOf("max_uses", 1)
                          .forGetter(GuildOffer::maxUses))
                  .apply(instance, GuildOffer::new));

  public TradeOffer toTradeOffer() {
    return new TradeOffer(wants, alsoWants, gives.copy(), maxUses, 0, 0.0F);
  }
}
