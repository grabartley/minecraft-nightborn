package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.Optional;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class TradePickTest {

  private static TradeOffer wheatForEmerald(final int uses) {
    final TradeOffer offer =
        new TradeOffer(new TradedItem(Items.WHEAT, 20), new ItemStack(Items.EMERALD), 12, 2, 0.05F);
    for (int use = 0; use < uses; use++) {
      offer.use();
    }
    return offer;
  }

  @ParameterizedTest(name = "{0} wheat, {1} uses: affordable {2}")
  @CsvSource({"20, 0, true", "64, 0, true", "19, 0, false", "64, 12, false"})
  @DisplayName("an offer is affordable with enough goods while it has uses left")
  void canBuy(final int wheat, final int uses, final boolean affordable) {
    final SimpleInventory carried = new SimpleInventory(HumanFamiliarEntity.CARRY_SLOTS);
    carried.setStack(0, new ItemStack(Items.WHEAT, wheat));
    assertEquals(affordable, TradePick.canBuy(wheatForEmerald(uses), carried));
  }

  @ParameterizedTest(name = "a full pack of {0}: affordable {1}")
  @CsvSource({"STONE, false", "EMERALD, true"})
  @DisplayName("an offer needs room for what it sells")
  void needsRoom(final String filler, final boolean affordable) {
    final SimpleInventory carried = new SimpleInventory(HumanFamiliarEntity.CARRY_SLOTS);
    carried.setStack(0, new ItemStack(Items.WHEAT, 20));
    final ItemStack fill =
        "STONE".equals(filler) ? new ItemStack(Items.STONE, 64) : new ItemStack(Items.EMERALD, 1);
    for (int slot = 1; slot < carried.size(); slot++) {
      carried.setStack(slot, fill.copy());
    }
    assertEquals(affordable, TradePick.canBuy(wheatForEmerald(0), carried));
  }

  @ParameterizedTest(name = "{0} wheat split over two slots")
  @CsvSource({"10, 10", "15, 30", "20, 0"})
  @DisplayName("paying takes the price from any slots and adds the goods")
  void pay(final int first, final int second) {
    final SimpleInventory carried = new SimpleInventory(HumanFamiliarEntity.CARRY_SLOTS);
    carried.setStack(0, new ItemStack(Items.WHEAT, first));
    carried.setStack(4, new ItemStack(Items.WHEAT, second));
    final TradeOffer offer = wheatForEmerald(0);
    assertTrue(TradePick.canBuy(offer, carried));
    assertTrue(TradePick.pay(offer, carried).isEmpty());
    assertEquals(first + second - 20, carried.count(Items.WHEAT));
    assertEquals(1, carried.count(Items.EMERALD));
  }

  @ParameterizedTest(name = "{0} wheat")
  @CsvSource({"19, false", "20, true"})
  @DisplayName("the first affordable offer in the list is picked")
  void affordable(final int wheat, final boolean found) {
    final TradeOfferList offers = new TradeOfferList();
    offers.add(
        new TradeOffer(
            new TradedItem(Items.PAPER, 24), new ItemStack(Items.EMERALD), 12, 2, 0.05F));
    final TradeOffer wanted = wheatForEmerald(0);
    offers.add(wanted);
    final SimpleInventory carried = new SimpleInventory(HumanFamiliarEntity.CARRY_SLOTS);
    carried.setStack(0, new ItemStack(Items.WHEAT, wheat));
    assertEquals(
        found ? Optional.of(wanted) : Optional.empty(), TradePick.affordable(offers, carried));
  }
}
