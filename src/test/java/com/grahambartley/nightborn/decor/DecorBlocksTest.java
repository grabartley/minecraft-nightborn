package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DecorBlocksTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets/nightborn");
  private static final Path DATA = Path.of("src/main/resources/data/nightborn");

  static Stream<String> familyIds() {
    return BuildingFamily.ALL.stream().flatMap(family -> family.ids().stream());
  }

  @ParameterizedTest(name = "{0} is registered with an item")
  @MethodSource("familyIds")
  void registersEveryFamilyMemberWithItsItem(final String id) {
    final Block block = DecorBlocks.get(id);
    assertEquals("nightborn:" + id, Registries.BLOCK.getId(block).toString());
    assertEquals(block, Block.getBlockFromItem(Registries.ITEM.get(Registries.BLOCK.getId(block))));
  }

  @ParameterizedTest(name = "{0} is a {1}")
  @CsvSource({
    "gothic_stone_brick_stairs, StairsBlock",
    "mossy_gothic_stone_brick_slab, SlabBlock",
    "cracked_gothic_stone_brick_wall, WallBlock",
    "ebony_fence, FenceBlock",
    "candelabra, CandelabraBlock",
    "chandelier, ChandelierBlock",
    "ornate_chair, ChairBlock",
    "ornate_table, TableBlock"
  })
  void buildsEachMemberFromTheMatchingVanillaShape(final String id, final String kind) {
    final Class<?> expected =
        switch (kind) {
          case "StairsBlock" -> StairsBlock.class;
          case "SlabBlock" -> SlabBlock.class;
          case "WallBlock" -> WallBlock.class;
          case "FenceBlock" -> FenceBlock.class;
          case "CandelabraBlock" -> CandelabraBlock.class;
          case "ChandelierBlock" -> ChandelierBlock.class;
          case "ChairBlock" -> ChairBlock.class;
          default -> TableBlock.class;
        };
    assertTrue(expected.isInstance(DecorBlocks.get(id)));
  }

  @Test
  void refusesAnUnknownId() {
    assertThrows(IllegalArgumentException.class, () -> DecorBlocks.get("velvet_coffin"));
  }

  static Stream<String> registeredIds() {
    return DecorBlocks.all().stream().map(block -> Registries.BLOCK.getId(block).getPath());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("registeredIds")
  void everyRegisteredBlockHasItsStateModelItemLootAndName(final String id) throws IOException {
    assertTrue(Files.exists(ASSETS.resolve("blockstates/" + id + ".json")), "blockstate");
    assertTrue(Files.exists(ASSETS.resolve("models/item/" + id + ".json")), "item model");
    assertTrue(Files.exists(DATA.resolve("loot_table/blocks/" + id + ".json")), "loot table");
    assertTrue(lang("en_us").has("block.nightborn." + id), "english name");
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"de_de", "es_es", "fr_fr", "pt_br", "ru_ru", "zh_cn"})
  void everyLanguageNamesWhatEnglishNames(final String language) throws IOException {
    assertEquals(lang("en_us").keySet(), lang(language).keySet());
  }

  private static JsonObject lang(final String language) throws IOException {
    return JsonParser.parseString(Files.readString(ASSETS.resolve("lang/" + language + ".json")))
        .getAsJsonObject();
  }
}
