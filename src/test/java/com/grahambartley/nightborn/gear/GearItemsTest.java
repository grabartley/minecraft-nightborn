package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
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
        Arguments.of("raw_silver", GearItems.RAW_SILVER, Item.class));
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

  static Stream<Arguments> ores() {
    return Stream.of(
        Arguments.of("silver_ore", GearItems.SILVER_ORE),
        Arguments.of("deepslate_silver_ore", GearItems.DEEPSLATE_SILVER_ORE));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("ores")
  @DisplayName("each ore is registered with its item, blockstate and model, and needs a tool")
  void ore(final String name, final Block ore) {
    assertEquals("nightborn:" + name, Registries.BLOCK.getId(ore).toString());
    assertEquals(ore, ((net.minecraft.item.BlockItem) ore.asItem()).getBlock());
    assertTrue(ore.getDefaultState().isToolRequired());
    assertTrue(Files.exists(ASSETS.resolve("blockstates/" + name + ".json")));
    assertTrue(Files.exists(ASSETS.resolve("models/block/" + name + ".json")));
  }
}
