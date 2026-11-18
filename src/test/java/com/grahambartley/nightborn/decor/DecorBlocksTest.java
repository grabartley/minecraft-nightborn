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
    "gothic_stone_brick_stairs, net.minecraft.block.StairsBlock",
    "mossy_gothic_stone_brick_slab, net.minecraft.block.SlabBlock",
    "cracked_gothic_stone_brick_wall, net.minecraft.block.WallBlock",
    "ebony_fence, net.minecraft.block.FenceBlock",
    "limewashed_brick_wall, net.minecraft.block.WallBlock",
    "clay_roof_tile_stairs, net.minecraft.block.StairsBlock",
    "clay_roof_tile_slab, net.minecraft.block.SlabBlock",
    "plaster, net.minecraft.block.Block",
    "candelabra, com.grahambartley.nightborn.decor.CandelabraBlock",
    "chandelier, com.grahambartley.nightborn.decor.ChandelierBlock",
    "ornate_chair, com.grahambartley.nightborn.decor.ChairBlock",
    "ornate_table, com.grahambartley.nightborn.decor.TableBlock",
    "blood_fountain, com.grahambartley.nightborn.decor.BloodFountainBlock",
    "blood_pool, com.grahambartley.nightborn.decor.BloodPoolBlock",
    "blood_splatter, com.grahambartley.nightborn.decor.BloodSplatterBlock",
    "chalice, com.grahambartley.nightborn.decor.ChaliceBlock",
    "weapon_rack, com.grahambartley.nightborn.decor.WeaponRackBlock",
    "wanted_poster, com.grahambartley.nightborn.decor.PictureBlock",
    "trophy_mount, com.grahambartley.nightborn.decor.WallDecorBlock",
    "portrait, com.grahambartley.nightborn.decor.PictureBlock",
    "gargoyle, com.grahambartley.nightborn.decor.StatueBlock",
    "ash_urn, com.grahambartley.nightborn.decor.StatueBlock",
    "curio_cabinet, com.grahambartley.nightborn.decor.StatueBlock"
  })
  void buildsEachMemberFromTheMatchingShape(final String id, final Class<?> kind) {
    assertTrue(kind.isInstance(DecorBlocks.get(id)));
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
