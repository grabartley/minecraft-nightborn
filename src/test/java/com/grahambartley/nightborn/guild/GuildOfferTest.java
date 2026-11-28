package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.rank.GuildRank;
import com.grahambartley.nightborn.rank.RankLadder;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

  @ParameterizedTest(name = "{0}")
  @MethodSource("offerNames")
  void everyShippedOfferReadsTheSameThroughItsMapCodec(final String name) throws IOException {
    final GuildOffer offer = GuildDataFiles.load("guild_offer", GuildOffer.CODEC).get(name);
    final GuildOffer again =
        GuildOffer.MAP_CODEC
            .codec()
            .parse(
                JsonOps.INSTANCE,
                GuildOffer.CODEC.encodeStart(JsonOps.INSTANCE, offer).getOrThrow())
            .getOrThrow();
    assertEquals(offer.order(), again.order());
    assertEquals(offer.maxUses(), again.maxUses());
    assertEquals(offer.rank(), again.rank());
    assertTrue(ItemStack.areEqual(offer.gives(), again.gives()));
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

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "'b:10, a:20, c:5', 'c, b, a'",
    "'b:0, a:0, c:0', 'a, b, c'",
    "'z:1, a:2, m:1', 'm, z, a'",
    "'only:7', 'only'"
  })
  void offersRunByOrderThenById(final String offers, final String expected) {
    final Map<Identifier, GuildOffer> byId = new java.util.HashMap<>();
    for (final String entry : offers.split(", ")) {
      final String[] parts = entry.split(":");
      byId.put(
          Identifier.of("nightborn", parts[0]),
          new GuildOffer(
              Integer.parseInt(parts[1]),
              new TradedItem(Items.EMERALD),
              Optional.empty(),
              new ItemStack(Items.PAPER, parts[0].length()),
              1,
              Optional.empty()));
    }
    assertEquals(
        java.util.Arrays.stream(expected.split(", ")).map(String::length).toList(),
        GuildOffer.inOrder(byId).stream().map(offer -> offer.gives().getCount()).toList());
  }

  @ParameterizedTest(name = "rank {0} -> needs {1}")
  @CsvSource({
    "'', 0",
    "nightborn:initiate, 0",
    "nightborn:hunter, 1",
    "nightborn:master, 3",
    "nightborn:lost, 16"
  })
  void anOfferNeedsTheRankItNames(final String rank, final int needs) {
    final RankLadder ladder =
        new RankLadder(
            Stream.of("initiate", "hunter", "veteran", "master")
                .map(path -> new RankLadder.Rung(Identifier.of("nightborn", path), null))
                .toList());
    final GuildOffer offer =
        new GuildOffer(
            0,
            new TradedItem(Items.EMERALD),
            Optional.empty(),
            new ItemStack(Items.PAPER),
            1,
            Optional.ofNullable(rank.isEmpty() ? null : Identifier.of(rank)));
    assertEquals(needs, offer.needs(ladder));
  }

  @Test
  void everyShippedRankedOfferNamesAShippedRank() throws IOException {
    final Map<String, GuildRank> ranks = GuildDataFiles.load("guild_rank", GuildRank.CODEC);
    GuildDataFiles.load("guild_offer", GuildOffer.CODEC)
        .values()
        .forEach(
            offer ->
                offer
                    .rank()
                    .ifPresent(
                        id -> assertTrue(ranks.containsKey(id.getPath()), id + " is not a rank")));
  }
}
