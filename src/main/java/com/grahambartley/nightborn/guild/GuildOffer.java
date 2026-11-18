package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.faction.Oath;
import com.grahambartley.nightborn.rank.RankLadder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;

public record GuildOffer(
    int order,
    TradedItem wants,
    Optional<TradedItem> alsoWants,
    ItemStack gives,
    int maxUses,
    Optional<Identifier> rank) {

  public static final int OUT_OF_REACH = Oath.TOP_RANK + 1;

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
                          .forGetter(GuildOffer::maxUses),
                      Identifier.CODEC.optionalFieldOf("rank").forGetter(GuildOffer::rank))
                  .apply(instance, GuildOffer::new));

  public static List<GuildOffer> inOrder(final Map<Identifier, GuildOffer> offers) {
    return offers.entrySet().stream()
        .sorted(
            Comparator.comparingInt(
                    (Map.Entry<Identifier, GuildOffer> entry) -> entry.getValue().order())
                .thenComparing(entry -> entry.getKey().toString()))
        .map(Map.Entry::getValue)
        .toList();
  }

  public int needs(final RankLadder ladder) {
    return rank.map(id -> ladder.rankOf(id).orElse(OUT_OF_REACH)).orElse(0);
  }

  public TradeOffer toTradeOffer() {
    return new TradeOffer(wants, alsoWants, gives.copy(), maxUses, 0, 0.0F);
  }
}
