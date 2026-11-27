package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class GearTagsTest {

  private static final Path DATA = Path.of("src/main/resources/data");
  private static final Path CHESTS = DATA.resolve("nightborn/loot_table/chests");

  private static JsonObject json(final Path file) {
    try {
      return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static Stream<String> items(final String tag) {
    final int split = tag.indexOf(':');
    return json(
            DATA.resolve(tag.substring(0, split))
                .resolve("tags/item")
                .resolve(tag.substring(split + 1) + ".json"))
        .getAsJsonArray("values")
        .asList()
        .stream()
        .map(GearTagsTest::id)
        .flatMap(value -> value.startsWith("#") ? items(value.substring(1)) : Stream.of(value));
  }

  private static String id(final JsonElement value) {
    return value.isJsonObject()
        ? value.getAsJsonObject().get("id").getAsString()
        : value.getAsString();
  }

  private static Set<String> antiVampire() {
    return items(GearTags.ANTI_VAMPIRE.id().toString()).collect(Collectors.toSet());
  }

  private static List<JsonObject> entries(final String table) {
    final List<JsonObject> out = new ArrayList<>();
    for (final JsonElement pool : json(CHESTS.resolve(table + ".json")).getAsJsonArray("pools")) {
      pool.getAsJsonObject()
          .getAsJsonArray("entries")
          .forEach(entry -> out.add(entry.getAsJsonObject()));
    }
    return out;
  }

  private static Stream<String> lootItems(final String table) {
    return entries(table).stream()
        .filter(entry -> entry.has("name"))
        .flatMap(
            entry ->
                entry.get("type").getAsString().equals("minecraft:tag")
                    ? items(entry.get("name").getAsString())
                    : Stream.of(entry.get("name").getAsString()));
  }

  static Stream<String> vampireTables() {
    return Stream.of("vampire_structure", "castle", "mountain_castle");
  }

  @Test
  @DisplayName("the reverse grip tag is the stakes tag, so a new stake thrusts with no code")
  void everyStake() {
    assertEquals("nightborn:reverse_grip", GearTags.REVERSE_GRIP.id().toString());
    assertEquals(
        List.of("#nightborn:stakes"),
        json(DATA.resolve("nightborn/tags/item/reverse_grip.json"))
            .getAsJsonArray("values")
            .asList()
            .stream()
            .map(JsonElement::getAsString)
            .toList());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"nightborn:wooden_stake", "nightborn:silver_stake", "nightborn:ashen_silver_stake"})
  @DisplayName("each stake holds the reverse grip")
  void stake(final String item) {
    assertEquals(List.of(item), items("nightborn:reverse_grip").filter(item::equals).toList());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "nightborn:wooden_stake",
    "nightborn:silver_stake",
    "nightborn:silver_sword",
    "nightborn:silver_axe",
    "nightborn:ashen_silver_sword",
    "nightborn:raw_silver",
    "nightborn:silver_ingot",
    "nightborn:silver_nugget",
    "nightborn:garlic",
    "nightborn:garlic_string",
    "nightborn:garlic_bomb",
    "nightborn:gold_crucifix",
    "nightborn:silver_crucifix",
    "nightborn:stake_bolt",
    "nightborn:silver_bolt",
    "nightborn:holy_water_bolt",
    "nightborn:stake_loops",
    "nightborn:stakewrights_gloves",
    "nightborn:silver_threaded_gloves",
    "nightborn:bandolier",
    "nightborn:flask_belt",
    "nightborn:crucifix_on_a_chain",
    "nightborn:garlic_sachet",
    "nightborn:sunburst_brooch",
    "nightborn:hunters_monocle",
    "nightborn:garlic_soaked_scarf"
  })
  @DisplayName("gear made to kill or ward off vampires is anti-vampire")
  void antiVampireGear(final String item) {
    assertEquals("nightborn:anti_vampire", GearTags.ANTI_VAMPIRE.id().toString());
    assertTrue(antiVampire().contains(item), item);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "nightborn:blood_bottle",
    "nightborn:chalice",
    "nightborn:candelabra",
    "nightborn:widows_ring",
    "nightborn:blessed_ring",
    "minecraft:gold_ingot",
    "minecraft:golden_apple"
  })
  @DisplayName("what a vampire house keeps is not anti-vampire")
  void houseKeeping(final String item) {
    assertFalse(antiVampire().contains(item), item);
  }

  @Test
  @DisplayName("every item in the anti-vampire tag is a registered item")
  void registered() {
    antiVampire()
        .forEach(item -> assertTrue(Registries.ITEM.containsId(Identifier.of(item)), item));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("vampireTables")
  @DisplayName("no vampire structure chest holds anti-vampire gear")
  void vampireChestsHoldNoHunterGear(final String table) {
    final Set<String> banned = antiVampire();
    assertEquals(List.of(), lootItems(table).filter(banned::contains).toList(), table);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("vampireTables")
  @DisplayName("no vampire structure chest holds holy water")
  void vampireChestsHoldNoHolyWater(final String table) {
    assertFalse(
        json(CHESTS.resolve(table + ".json")).toString().contains("nightborn:holy_water"), table);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "nightborn:wooden_stake",
    "nightborn:silver_stake",
    "nightborn:stake_bolt",
    "nightborn:garlic",
    "nightborn:garlic_bomb",
    "nightborn:silver_nugget",
    "nightborn:crucifix_on_a_chain",
    "nightborn:hunters_monocle"
  })
  @DisplayName("a hunter camp chest still holds hunter gear")
  void hunterCampKeepsItsGear(final String item) {
    assertTrue(antiVampire().contains(item), item);
    assertTrue(lootItems("hunter_camp").anyMatch(item::equals), item);
  }
}
