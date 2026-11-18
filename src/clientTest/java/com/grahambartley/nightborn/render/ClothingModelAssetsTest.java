package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.clothing.ClothingCut;
import com.grahambartley.nightborn.clothing.ClothingMotion;
import com.grahambartley.nightborn.clothing.ClothingPiece;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.item.ArmorItem;
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
    assertTrue(Files.exists(GeoAssets.mainFile(ClothingArt.texture(piece.id(), cut))));
    final Set<String> bones = GeoAssets.boneNames(GeoAssets.file(ClothingArt.geo(piece.id(), cut)));
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
        GeoAssets.animations(GeoAssets.file(ClothingArt.animation(piece.id()))).keySet());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("accessories")
  @DisplayName("every drawn accessory ships both cuts and its motion clips")
  void drawnAccessoriesShipTheirArt(final String id) throws IOException {
    if (!Files.exists(GeoAssets.file(ClothingArt.geo(id, ClothingCut.MALE)))) {
      return;
    }
    for (final ClothingCut cut : ClothingCut.values()) {
      assertTrue(Files.exists(GeoAssets.file(ClothingArt.geo(id, cut))));
      assertTrue(Files.exists(GeoAssets.mainFile(ClothingArt.texture(id, cut))));
    }
    assertEquals(
        Set.of(ClothingMotion.IDLE.clip(), ClothingMotion.WALK.clip()),
        GeoAssets.animations(GeoAssets.file(ClothingArt.animation(id))).keySet());
  }

  static java.util.stream.Stream<String> accessories() throws IOException {
    try (java.util.stream.Stream<java.nio.file.Path> files =
        Files.list(
            java.nio.file.Path.of("src/main/resources/data/nightborn/nightborn/accessory"))) {
      return files
          .map(file -> file.getFileName().toString().replace(".json", ""))
          .toList()
          .stream();
    }
  }
}
