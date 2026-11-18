package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PickaxeRankTest {

  @ParameterizedTest(name = "{0} ranks below {1}")
  @CsvSource({
    "golden_pickaxe, wooden_pickaxe",
    "wooden_pickaxe, stone_pickaxe",
    "stone_pickaxe, iron_pickaxe",
    "iron_pickaxe, diamond_pickaxe",
    "diamond_pickaxe, netherite_pickaxe",
    "wheat, wooden_pickaxe"
  })
  @DisplayName("a better pickaxe ranks higher, so a servant reaches for the one that mines most")
  void ranks(final String worse, final String better) {
    assertTrue(
        PickaxeRank.of(stack(worse)) < PickaxeRank.of(stack(better)),
        worse + " should rank below " + better);
  }

  private static ItemStack stack(final String name) {
    return new ItemStack(
        net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.ofVanilla(name)));
  }
}
