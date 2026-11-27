package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.EquipmentSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class AccessoryAssetsTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets/nightborn");
  private static final Path CLIENT = Path.of("src/client/resources/assets/nightborn");
  private static final Path DATA = Path.of("src/main/resources/data");
  private static final Map<EquipmentSlot, Set<String>> ROOT_BONES =
      Map.of(
          EquipmentSlot.HEAD, Set.of("armorHead"),
          EquipmentSlot.CHEST, Set.of("armorBody", "armorRightArm", "armorLeftArm"));

  @Test
  @DisplayName("a player is given exactly the eight accessory slots")
  void playerSlots() throws IOException {
    final JsonObject entities = read("trinkets/entities/nightborn.json");
    assertEquals(List.of("player"), strings(entities.getAsJsonArray("entities")));
    assertEquals(
        Arrays.stream(AccessorySlot.values()).map(AccessorySlot::id).toList(),
        strings(entities.getAsJsonArray("slots")));
  }

  @Test
  @DisplayName("the brooch slot, which no shipped slot covers, is declared with its own icon")
  void broochSlot() throws IOException {
    assertEquals(
        "nightborn:gui/slots/brooch",
        read("trinkets/slots/chest/brooch.json").get("icon").getAsString());
    assertTrue(Files.exists(ASSETS.resolve("textures/gui/slots/brooch.png")));
  }

  @ParameterizedTest(name = "{0} fits {1}")
  @CsvSource({
    "mourning_veil, head/face",
    "high_collared_cape, chest/back",
    "onyx_ring, hand/ring",
    "velvet_choker, chest/necklace",
    "cameo_brooch, chest/brooch",
    "lace_gloves, hand/glove",
    "brass_buckled_belt, legs/belt"
  })
  @DisplayName("each piece is tagged into one slot only")
  void eachPieceHasOneSlot(final String piece, final String slot) throws IOException {
    final Set<String> slots = new HashSet<>();
    for (final AccessorySlot each : AccessorySlot.values()) {
      if (strings(read("trinkets/tags/item/" + each.id() + ".json").getAsJsonArray("values"))
          .contains("nightborn:" + piece)) {
        slots.add(each.id());
      }
    }
    assertEquals(Set.of(slot), slots);
  }

  @Test
  @DisplayName("the second ring slot takes whatever the first takes")
  void secondRing() throws IOException {
    assertEquals(
        List.of("#trinkets:hand/ring"),
        strings(read("trinkets/tags/item/offhand/ring.json").getAsJsonArray("values")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("com.grahambartley.nightborn.accessory.AccessoriesTest#ids")
  @DisplayName("each piece ships its sprite and, unless it is a ring, its worn model")
  void shipsItsFiles(final String piece) throws IOException {
    assertTrue(Files.exists(ASSETS.resolve("models/item/" + piece + ".json")));
    assertTrue(Files.exists(ASSETS.resolve("textures/item/" + piece + ".png")));
    final boolean drawn = slotOf(piece).drawn();
    for (final String cut : List.of("male", "female")) {
      assertEquals(
          drawn, Files.exists(CLIENT.resolve("geo/armor/" + piece + "_" + cut + ".geo.json")));
      assertEquals(
          drawn, Files.exists(ASSETS.resolve("textures/armor/" + piece + "_" + cut + ".png")));
    }
    assertEquals(
        drawn, Files.exists(CLIENT.resolve("animations/armor/" + piece + ".animation.json")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("com.grahambartley.nightborn.accessory.AccessoriesTest#ids")
  @DisplayName("a plain piece is crafted, and a piece with a gift is only ever found")
  void craftedOnlyWhenPlain(final String piece) throws IOException {
    final JsonObject data = read("nightborn/nightborn/accessory/" + piece + ".json");
    final boolean plain = !data.has("gifts") && !data.has("holds");
    assertEquals(plain, Files.exists(DATA.resolve("nightborn/recipe/" + piece + ".json")), piece);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("com.grahambartley.nightborn.accessory.AccessoriesTest#ids")
  @DisplayName("a worn model hangs only from the armour bones of the body part it is drawn on")
  void rootBonesFitTheBodyPart(final String piece) throws IOException {
    final AccessorySlot slot = slotOf(piece);
    if (!slot.drawn()) {
      return;
    }
    final Set<String> allowed = ROOT_BONES.get(slot.drawnOn());
    for (final String cut : List.of("male", "female")) {
      final JsonObject geometry =
          JsonParser.parseString(
                  Files.readString(CLIENT.resolve("geo/armor/" + piece + "_" + cut + ".geo.json")))
              .getAsJsonObject()
              .getAsJsonArray("minecraft:geometry")
              .get(0)
              .getAsJsonObject();
      for (final JsonElement bone : geometry.getAsJsonArray("bones")) {
        final JsonObject each = bone.getAsJsonObject();
        if (!each.has("parent")) {
          assertTrue(allowed.contains(each.get("name").getAsString()), piece + " " + cut);
        }
      }
    }
  }

  private static AccessorySlot slotOf(final String piece) throws IOException {
    for (final AccessorySlot slot : AccessorySlot.values()) {
      if (strings(read("trinkets/tags/item/" + slot.id() + ".json").getAsJsonArray("values"))
          .contains("nightborn:" + piece)) {
        return slot;
      }
    }
    throw new AssertionError(piece + " is in no slot");
  }

  private static JsonObject read(final String path) throws IOException {
    return JsonParser.parseString(Files.readString(DATA.resolve(path))).getAsJsonObject();
  }

  private static List<String> strings(final Iterable<JsonElement> values) {
    final java.util.ArrayList<String> out = new java.util.ArrayList<>();
    values.forEach(value -> out.add(value.getAsString()));
    return out;
  }

  @ParameterizedTest(name = "{0} in {1}")
  @CsvSource({
    "cloak_of_dusk, castle, castle",
    "nightgliders_cloak, mountain_castle, mountain_castle",
    "pilgrims_cloak, vampire_structure, crypt",
    "stalkers_cloak, vampire_structure, ruined_manor",
    "travellers_cloak, vampire_structure, underground_lair",
    "clawed_gauntlets, vampire_structure, underground_lair",
    "silk_gloves, vampire_structure, ruined_manor",
    "stakewrights_gloves, hunter_camp, hunter_camp",
    "silver_threaded_gloves, hunter_camp, hunter_camp",
    "gravediggers_mitts, vampire_structure, crypt",
    "cellarers_belt, vampire_structure, ruined_manor",
    "bandolier, hunter_camp, hunter_camp",
    "stake_loops, hunter_camp, hunter_camp",
    "flask_belt, hunter_camp, hunter_camp",
    "sturdy_girdle, vampire_structure, ruined_manor",
    "widows_veil, castle, castle",
    "smoked_spectacles, vampire_structure, ruined_manor",
    "porcelain_mask, mountain_castle, mountain_castle",
    "hunters_monocle, hunter_camp, hunter_camp",
    "confessors_veil, vampire_structure, crypt",
    "garlic_soaked_scarf, hunter_camp, hunter_camp",
    "plague_mask, vampire_structure, crypt",
    "crucifix_on_a_chain, hunter_camp, hunter_camp",
    "garlic_sachet, hunter_camp, hunter_camp",
    "sunburst_brooch, hunter_camp, hunter_camp"
  })
  @DisplayName("each found piece is in the chests of the structure that holds it, and only there")
  void foundWhereItBelongs(final String piece, final String table, final String structure)
      throws IOException {
    final List<String> places = new java.util.ArrayList<>();
    for (final JsonElement pool :
        read("nightborn/loot_table/chests/" + table + ".json").getAsJsonArray("pools")) {
      for (final JsonElement entry : pool.getAsJsonObject().getAsJsonArray("entries")) {
        final JsonObject item = entry.getAsJsonObject();
        if (item.has("name") && item.get("name").getAsString().equals("nightborn:" + piece)) {
          places.add(
              item.has("conditions")
                  ? item.getAsJsonArray("conditions")
                      .get(0)
                      .getAsJsonObject()
                      .getAsJsonObject("predicate")
                      .get("structures")
                      .getAsString()
                  : "nightborn:" + table);
        }
      }
    }
    assertEquals(List.of("nightborn:" + structure), places);
    try (var tables = Files.list(DATA.resolve("nightborn/loot_table/chests"))) {
      for (final Path other : tables.toList()) {
        if (!other.getFileName().toString().equals(table + ".json")) {
          assertFalse(
              Files.readString(other).contains("\"nightborn:" + piece + "\""),
              piece + " is also in " + other.getFileName());
        }
      }
    }
  }

  @ParameterizedTest(name = "{0} pays {1}")
  @CsvSource({
    "trial/not_a_sound, wardens_cape",
    "trial/old_grudge, death_mask",
    "trial/seen_through, lantern_lens_goggles",
    "trial/sprung, trappers_pouch",
    "feat/a_full_table, lords_mantle",
    "feat/a_ghost_at_the_feast, opera_mask"
  })
  @DisplayName("a trial or feat that names a piece pays one that exists")
  void rewardsExist(final String challenge, final String piece) throws IOException {
    assertEquals(
        "nightborn:" + piece,
        read("nightborn/nightborn/" + challenge + ".json").get("reward").getAsString());
    assertTrue(AccessoryItems.IDS.contains(piece));
  }

  @Test
  @DisplayName("no two shapeless recipes take the same ingredients, so each piece has its own")
  void shapelessRecipesDiffer() throws IOException {
    final Set<List<String>> seen = new HashSet<>();
    try (var files = Files.list(DATA.resolve("nightborn/recipe"))) {
      for (final Path file : files.toList()) {
        final JsonObject recipe = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        if (recipe.get("type").getAsString().equals("minecraft:crafting_shapeless")) {
          final List<String> ingredients = new java.util.ArrayList<>();
          recipe.getAsJsonArray("ingredients").forEach(i -> ingredients.add(i.toString()));
          ingredients.sort(null);
          assertTrue(seen.add(ingredients), file.getFileName() + " repeats " + ingredients);
        }
      }
    }
  }
}
