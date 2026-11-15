package com.grahambartley.nightborn.holy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.ammo.AmmoItems;
import java.util.stream.Stream;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.village.TradeOffer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HolyWaterTest {

  static Stream<Arguments> stacks() {
    return Stream.of(
        Arguments.of("holy water", HolyWater.bottle(), true),
        Arguments.of(
            "splash holy water",
            PotionContentsComponent.createStack(Items.SPLASH_POTION, HolyWater.POTION),
            true),
        Arguments.of(
            "lingering holy water",
            PotionContentsComponent.createStack(Items.LINGERING_POTION, HolyWater.POTION),
            true),
        Arguments.of("holy water bolt", new ItemStack(AmmoItems.HOLY_WATER_BOLT), true),
        Arguments.of(
            "water bottle",
            PotionContentsComponent.createStack(Items.POTION, Potions.WATER),
            false),
        Arguments.of(
            "harming", PotionContentsComponent.createStack(Items.POTION, Potions.HARMING), false),
        Arguments.of("stake bolt", new ItemStack(AmmoItems.STAKE_BOLT), false),
        Arguments.of("empty", ItemStack.EMPTY, false));
  }

  @ParameterizedTest(name = "{0}: {2}")
  @MethodSource("stacks")
  @DisplayName("holy water is told apart by what it carries, whatever holds it")
  void isHolyWater(final String name, final ItemStack stack, final boolean holy) {
    assertEquals(holy, HolyWater.isHolyWater(stack));
  }

  static Stream<Arguments> offered() {
    return Stream.of(
        Arguments.of(
            "water bottle", PotionContentsComponent.createStack(Items.POTION, Potions.WATER), true),
        Arguments.of(
            "awkward potion",
            PotionContentsComponent.createStack(Items.POTION, Potions.AWKWARD),
            false),
        Arguments.of("glass bottle", new ItemStack(Items.GLASS_BOTTLE), false),
        Arguments.of(
            "splash water",
            PotionContentsComponent.createStack(Items.SPLASH_POTION, Potions.WATER),
            false));
  }

  @ParameterizedTest(name = "{0}: {2}")
  @MethodSource("offered")
  @DisplayName("a cleric blesses only a plain water bottle")
  void blessesWater(final String name, final ItemStack bottle, final boolean accepted) {
    final TradeOffer offer = HolyWater.blessing();
    assertEquals(
        accepted, offer.getSecondBuyItem().map(wanted -> wanted.matches(bottle)).orElse(false));
  }

  @Test
  @DisplayName("the blessing costs an emerald and gives one holy water")
  void blessingTerms() {
    final TradeOffer offer = HolyWater.blessing();
    assertTrue(offer.getFirstBuyItem().matches(new ItemStack(Items.EMERALD)));
    assertEquals(HolyWater.BLESSING_PRICE, offer.getFirstBuyItem().count());
    assertTrue(HolyWater.isHolyWater(offer.getSellItem()));
    assertTrue(offer.getSellItem().isOf(Items.POTION));
  }
}
