package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.clothing.ClothingCut;
import com.grahambartley.nightborn.clothing.ClothingMotion;
import com.grahambartley.nightborn.clothing.ClothingPiece;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class ClothingModelAssetsTest {

  private static final Map<ArmorItem.Type, Set<String>> SLOT_BONES =
      Map.of(
          ArmorItem.Type.HELMET, Set.of("armorHead"),
          ArmorItem.Type.CHESTPLATE, Set.of("armorBody", "armorRightArm", "armorLeftArm"),
          ArmorItem.Type.LEGGINGS, Set.of("armorRightLeg", "armorLeftLeg"),
          ArmorItem.Type.BOOTS, Set.of("armorRightBoot", "armorLeftBoot"));

  static Stream<Arguments> art() {
    return Stream.of(ClothingPiece.values())
        .flatMap(piece -> Stream.of(ClothingCut.values()).map(cut -> Arguments.of(piece, cut)));
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("art")
  @DisplayName("every piece ships each cut on the armour bones of its slot")
  void eachCutShipsOnItsSlotBones(final ClothingPiece piece, final ClothingCut cut)
      throws IOException {
    assertTrue(Files.exists(file(ClothingArt.texture(piece, cut), "src/main/resources")));
    final Set<String> bones = new HashSet<>();
    for (final JsonElement bone :
        read(ClothingArt.geo(piece, cut))
            .getAsJsonArray("minecraft:geometry")
            .get(0)
            .getAsJsonObject()
            .getAsJsonArray("bones")) {
      bones.add(bone.getAsJsonObject().get("name").getAsString());
    }
    assertTrue(bones.containsAll(SLOT_BONES.get(piece.type())), bones.toString());
    assertTrue(
        bones.stream().anyMatch(ClothingArt::isDyed) || !piece.dyeable(),
        "dyeable piece has a dyed bone");
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(ClothingPiece.class)
  @DisplayName("every piece ships the clips its wearer's motion plays")
  void shipsTheMotionClips(final ClothingPiece piece) throws IOException {
    assertEquals(
        Set.of(ClothingMotion.IDLE.clip(), ClothingMotion.WALK.clip()),
        read(ClothingArt.animation(piece)).getAsJsonObject("animations").keySet());
  }

  private static JsonObject read(final Identifier id) throws IOException {
    return JsonParser.parseString(Files.readString(file(id, "src/client/resources")))
        .getAsJsonObject();
  }

  private static Path file(final Identifier id, final String root) {
    return Path.of(root, "assets", id.getNamespace(), id.getPath());
  }
}
