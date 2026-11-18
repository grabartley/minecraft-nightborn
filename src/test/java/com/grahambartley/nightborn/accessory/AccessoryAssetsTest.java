package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class AccessoryAssetsTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets/nightborn");
  private static final Path CLIENT = Path.of("src/client/resources/assets/nightborn");
  private static final Path DATA = Path.of("src/main/resources/data");

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
  @DisplayName("each piece ships its sprite, recipe and, unless it is a ring, its worn model")
  void shipsItsFiles(final String piece) throws IOException {
    assertTrue(Files.exists(ASSETS.resolve("models/item/" + piece + ".json")));
    assertTrue(Files.exists(ASSETS.resolve("textures/item/" + piece + ".png")));
    assertTrue(Files.exists(DATA.resolve("nightborn/recipe/" + piece + ".json")));
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
}
