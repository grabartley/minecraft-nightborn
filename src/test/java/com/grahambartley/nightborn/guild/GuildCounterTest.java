package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GuildCounterTest {

  @ParameterizedTest(name = "{0}, contract {1} -> {2}")
  @CsvSource({
    "GEAR, false, true",
    "GEAR, true, false",
    "CONTRACTS, true, true",
    "CONTRACTS, false, false"
  })
  @DisplayName("gear and contracts are offered at separate counters, so each can be gated alone")
  void holds(final GuildCounter.Kind kind, final boolean contract, final boolean holds) {
    assertEquals(holds, kind.holds(contract));
  }

  @ParameterizedTest(name = "{0} -> {1}, {2}")
  @CsvSource({
    "GEAR, nightborn.guild.gear, nightborn:wheel/trade",
    "CONTRACTS, nightborn.guild.contracts, nightborn:wheel/contracts"
  })
  @DisplayName("each counter has its own label and icon on the wheel")
  void wheel(final GuildCounter.Kind kind, final String key, final String icon) {
    assertEquals(key, kind.translationKey());
    assertEquals(icon, kind.icon().toString());
  }

  @ParameterizedTest(name = "{0}, vampire {1}, hunter {2} -> {3}")
  @CsvSource({
    "GEAR, false, false, true",
    "GEAR, false, true, true",
    "GEAR, true, false, false",
    "CONTRACTS, false, false, false",
    "CONTRACTS, false, true, true",
    "CONTRACTS, true, false, false"
  })
  @DisplayName("any human may buy gear, but only a sworn hunter may take contracts")
  void openTo(
      final GuildCounter.Kind kind,
      final boolean vampire,
      final boolean hunter,
      final boolean open) {
    assertEquals(open, kind.openTo(vampire, hunter));
  }

  @ParameterizedTest(name = "needs {0}, guild rank {1} -> shown {2}")
  @CsvSource({
    "'0, 1, 2', 0, '1'",
    "'0, 1, 2', 1, '1, 2'",
    "'0, 1, 2', 3, '1, 2, 3'",
    "'2, 0, 16', 2, '1, 2'",
    "'0, 0', 0, '1, 2'",
    "'0', 3, '1'"
  })
  @DisplayName("a buyer sees the stock of their own guild rank and every rank below")
  void tiers(final String needs, final int guildRank, final String shown) {
    final TradeOfferList all = new TradeOfferList();
    for (int count = 1; count <= 3; count++) {
      all.add(
          new TradeOffer(
              new TradedItem(Items.EMERALD), new ItemStack(Items.PAPER, count), 1, 0, 0.0F));
    }
    final List<Integer> needed = Arrays.stream(needs.split(", ")).map(Integer::parseInt).toList();
    assertEquals(
        Arrays.stream(shown.split(", ")).map(Integer::parseInt).toList(),
        GuildCounter.Kind.GEAR.from(all, needed, guildRank).stream()
            .map(offer -> offer.getSellItem().getCount())
            .toList());
  }
}
