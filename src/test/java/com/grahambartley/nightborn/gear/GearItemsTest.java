package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class GearItemsTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets/nightborn");

  static Stream<Arguments> items() {
    return Stream.of(
        Arguments.of("wooden_stake", GearItems.WOODEN_STAKE, StakeItem.class),
        Arguments.of("silver_stake", GearItems.SILVER_STAKE, StakeItem.class),
        Arguments.of("silver_sword", GearItems.SILVER_SWORD, SwordItem.class),
        Arguments.of("silver_axe", GearItems.SILVER_AXE, AxeItem.class),
        Arguments.of("silver_ingot", GearItems.SILVER_INGOT, Item.class),
        Arguments.of("silver_nugget", GearItems.SILVER_NUGGET, Item.class),
        Arguments.of("raw_silver", GearItems.RAW_SILVER, Item.class),
        Arguments.of("garlic", GearItems.GARLIC, AliasedBlockItem.class),
        Arguments.of("gold_crucifix", GearItems.GOLD_CRUCIFIX, CrucifixItem.class),
        Arguments.of("silver_crucifix", GearItems.SILVER_CRUCIFIX, CrucifixItem.class));
  }

  static Stream<Arguments> crucifixes() {
    return Stream.of(
        Arguments.of("gold_crucifix", GearItems.GOLD_CRUCIFIX),
        Arguments.of("silver_crucifix", GearItems.SILVER_CRUCIFIX));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("crucifixes")
  @DisplayName("a crucifix is held one at a time")
  void crucifixDoesNotStack(final String name, final Item crucifix) {
    assertEquals(1, crucifix.getMaxCount());
  }

  @Test
  @DisplayName("garlic is planted as the garlic crop")
  void garlicPlantsTheCrop() {
    assertEquals(GearBlocks.GARLIC, ((AliasedBlockItem) GearItems.GARLIC).getBlock());
    assertEquals(GearItems.GARLIC, GearBlocks.GARLIC.asItem());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("items")
  @DisplayName("each item is registered at the id its art was made for")
  void registered(final String name, final Item item, final Class<? extends Item> type) {
    assertEquals("nightborn:" + name, Registries.ITEM.getId(item).toString());
    assertInstanceOf(type, item);
    assertTrue(Files.exists(ASSETS.resolve("models/item/" + name + ".json")));
    assertTrue(Files.exists(ASSETS.resolve("textures/item/" + name + ".png")));
  }
}
