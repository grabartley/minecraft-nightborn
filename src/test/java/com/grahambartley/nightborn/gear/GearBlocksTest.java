package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class GearBlocksTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets/nightborn");
  private static final Path DATA = Path.of("src/main/resources/data/nightborn");

  static Stream<Arguments> ores() {
    return Stream.of(
        Arguments.of("silver_ore", GearBlocks.SILVER_ORE),
        Arguments.of("deepslate_silver_ore", GearBlocks.DEEPSLATE_SILVER_ORE));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("ores")
  @DisplayName("each ore is registered with its item, blockstate and model, and needs a tool")
  void ore(final String name, final Block ore) {
    assertEquals("nightborn:" + name, Registries.BLOCK.getId(ore).toString());
    assertEquals(ore, ((BlockItem) ore.asItem()).getBlock());
    assertTrue(ore.getDefaultState().isToolRequired());
    assertTrue(Files.exists(ASSETS.resolve("blockstates/" + name + ".json")));
    assertTrue(Files.exists(ASSETS.resolve("models/block/" + name + ".json")));
  }

  static Stream<Arguments> garlicBlocks() {
    return Stream.of(
        Arguments.of("garlic", GearBlocks.GARLIC, "garlic_stage3"),
        Arguments.of("garlic_string", GearBlocks.GARLIC_STRING, "garlic_string_wall"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("garlicBlocks")
  @DisplayName("each garlic block is registered with its blockstate and the art made for it")
  void garlic(final String name, final Block block, final String model) {
    assertEquals("nightborn:" + name, Registries.BLOCK.getId(block).toString());
    assertTrue(Files.exists(ASSETS.resolve("blockstates/" + name + ".json")));
    assertTrue(Files.exists(ASSETS.resolve("models/block/" + model + ".json")));
    assertTrue(Files.exists(ASSETS.resolve("models/item/" + name + ".json")));
  }

  static Stream<Arguments> wallCrucifixes() {
    return Stream.of(
        Arguments.of("gold_wall_crucifix", GearBlocks.GOLD_WALL_CRUCIFIX, GearItems.GOLD_CRUCIFIX),
        Arguments.of(
            "silver_wall_crucifix", GearBlocks.SILVER_WALL_CRUCIFIX, GearItems.SILVER_CRUCIFIX));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("wallCrucifixes")
  @DisplayName("a hung crucifix is placed by, picked as and drops its own crucifix")
  void wallCrucifix(final String name, final Block block, final Item crucifix) {
    assertEquals("nightborn:" + name, Registries.BLOCK.getId(block).toString());
    assertEquals(crucifix, block.asItem());
    assertTrue(Files.exists(ASSETS.resolve("blockstates/" + name + ".json")));
    assertTrue(Files.exists(ASSETS.resolve("models/block/" + name + ".json")));
    assertTrue(Files.exists(DATA.resolve("loot_table/blocks/" + name + ".json")));
  }
}
