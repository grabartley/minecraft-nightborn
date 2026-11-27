package com.grahambartley.nightborn.wild;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

public final class RumourMaps {

  public static final String NAME_KEY = "filled_map.nightborn.vampire_house";
  static final int LEVEL = 1;
  static final int PRICE = 13;
  static final int USES = 12;
  static final int XP = 5;

  private RumourMaps() {}

  static void register() {
    TradeOfferHelper.registerVillagerOffers(
        VillagerProfession.CARTOGRAPHER, LEVEL, factories -> factories.add(map()));
    TradeOfferHelper.registerVillagerOffers(
        VillagerProfession.CLERIC, LEVEL, factories -> factories.add(map()));
  }

  static TradeOffers.Factory map() {
    return new RumourMapOffer(PRICE, USES, XP);
  }
}
