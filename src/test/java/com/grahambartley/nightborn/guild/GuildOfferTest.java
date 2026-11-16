package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.village.TradeOffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class GuildOfferTest {

  static Stream<String> offerNames() throws IOException {
    return GuildDataFiles.load("guild_offer", GuildOffer.CODEC).keySet().stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("offerNames")
  void everyShippedOfferBecomesAFreshTrade(final String name) throws IOException {
    final GuildOffer offer = GuildDataFiles.load("guild_offer", GuildOffer.CODEC).get(name);
    final TradeOffer trade = offer.toTradeOffer();
    assertEquals(offer.maxUses(), trade.getMaxUses());
    assertEquals(0, trade.getUses());
    assertTrue(ItemStack.areEqual(offer.gives(), trade.getSellItem()));
    final int given = offer.gives().getCount();
    trade.getSellItem().setCount(given + 1);
    assertEquals(given, offer.gives().getCount());
  }

  @Test
  void eachShippedContractIsOnOfferOnceADay() throws IOException {
    final Map<String, GuildOffer> offers = GuildDataFiles.load("guild_offer", GuildOffer.CODEC);
    final long contracts =
        offers.values().stream()
            .filter(offer -> offer.gives().contains(ContractItems.PROGRESS))
            .peek(offer -> assertEquals(1, offer.maxUses()))
            .count();
    assertEquals(GuildDataFiles.load("hunter_contract", HunterContract.CODEC).size(), contracts);
  }
}
