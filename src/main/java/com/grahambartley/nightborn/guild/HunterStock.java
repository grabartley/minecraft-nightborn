package com.grahambartley.nightborn.guild;

import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;

public final class HunterStock {

  static final String OFFERS_KEY = "Wares";
  static final String DAY_KEY = "WaresDay";

  private TradeOfferList offers = new TradeOfferList();
  private long stockedDay;

  public TradeOfferList offers() {
    return offers;
  }

  public boolean restock(final long day, final Supplier<List<TradeOffer>> fresh) {
    if (!offers.isEmpty() && !GuildRestock.due(stockedDay, day)) {
      return false;
    }
    offers = new TradeOfferList();
    offers.addAll(fresh.get());
    stockedDay = day;
    return true;
  }

  public void write(final NbtCompound nbt, final DynamicOps<NbtElement> ops) {
    TradeOfferList.CODEC
        .encodeStart(ops, offers)
        .result()
        .ifPresent(saved -> nbt.put(OFFERS_KEY, saved));
    nbt.putLong(DAY_KEY, stockedDay);
  }

  public void read(final NbtCompound nbt, final DynamicOps<NbtElement> ops) {
    offers =
        nbt.contains(OFFERS_KEY)
            ? TradeOfferList.CODEC
                .parse(ops, nbt.get(OFFERS_KEY))
                .result()
                .orElseGet(TradeOfferList::new)
            : new TradeOfferList();
    stockedDay = nbt.getLong(DAY_KEY);
  }
}
