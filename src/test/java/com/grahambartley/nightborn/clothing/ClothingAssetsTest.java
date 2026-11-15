package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.item.ArmorItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ClothingAssetsTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets/nightborn");
  private static final Path DATA = Path.of("src/main/resources/data");
  private static final Map<ArmorItem.Type, String> SLOT_TAGS =
      Map.of(
          ArmorItem.Type.HELMET, "head",
          ArmorItem.Type.CHESTPLATE, "chest",
          ArmorItem.Type.LEGGINGS, "legs",
          ArmorItem.Type.BOOTS, "feet");
  private static final Map<ArmorItem.Type, String> VANILLA_SLOT_TAGS =
      Map.of(
          ArmorItem.Type.HELMET, "head_armor",
          ArmorItem.Type.CHESTPLATE, "chest_armor",
          ArmorItem.Type.LEGGINGS, "leg_armor",
          ArmorItem.Type.BOOTS, "foot_armor");

  @ParameterizedTest(name = "{0}")
  @EnumSource(ClothingPiece.class)
  @DisplayName("each piece ships its model, sprite and recipe, and a dye overlay only if dyeable")
  void shipsItsFiles(final ClothingPiece piece) {
    assertTrue(Files.exists(ASSETS.resolve("models/item/" + piece.id() + ".json")));
    assertTrue(Files.exists(ASSETS.resolve("textures/item/" + piece.id() + ".png")));
    assertEquals(
        piece.dyeable(),
        Files.exists(ASSETS.resolve("textures/item/" + piece.id() + "_overlay.png")));
    assertTrue(Files.exists(DATA.resolve("nightborn/recipe/" + piece.id() + ".json")));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = ArmorItem.Type.class,
      names = {"HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS"})
  @DisplayName(
      "each slot's clothing tag lists exactly that slot's pieces, and vanilla's slot tag holds it")
  void slotTagsMatchThePieces(final ArmorItem.Type type) throws IOException {
    final String clothingTag =
        "nightborn/tags/item/enchantable/" + SLOT_TAGS.get(type) + "_clothing.json";
    assertEquals(
        Arrays.stream(ClothingPiece.values())
            .filter(piece -> piece.type() == type)
            .map(piece -> "nightborn:" + piece.id())
            .collect(Collectors.toSet()),
        values(clothingTag));
    assertEquals(
        Set.of("#nightborn:enchantable/" + SLOT_TAGS.get(type) + "_clothing"),
        values("minecraft/tags/item/" + VANILLA_SLOT_TAGS.get(type) + ".json"));
  }

  @Test
  @DisplayName("vanilla's dyeable tag holds exactly the dyeable pieces")
  void dyeableTagMatchesThePieces() throws IOException {
    assertEquals(
        Arrays.stream(ClothingPiece.values())
            .filter(ClothingPiece::dyeable)
            .map(piece -> "nightborn:" + piece.id())
            .collect(Collectors.toSet()),
        values("minecraft/tags/item/dyeable.json"));
  }

  private static Set<String> values(final String tag) throws IOException {
    final Set<String> values = new HashSet<>();
    for (final JsonElement value :
        JsonParser.parseString(Files.readString(DATA.resolve(tag)))
            .getAsJsonObject()
            .getAsJsonArray("values")) {
      values.add(value.getAsString());
    }
    return values;
  }
}
