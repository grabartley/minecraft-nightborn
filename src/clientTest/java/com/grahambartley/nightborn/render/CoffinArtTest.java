package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.coffin.CoffinStyle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class CoffinArtTest {

  private static final double HALF_PIXEL = 0.5 / 16.0;
  private static final Identifier SOURCE = Identifier.of("nightborn", "textures/block/source.png");

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "minecraft:oak_planks, minecraft:textures/block/oak_planks.png",
    "minecraft:dark_oak_planks, minecraft:textures/block/dark_oak_planks.png",
    "minecraft:crimson_planks, minecraft:textures/block/crimson_planks.png",
    "minecraft:deepslate_bricks, minecraft:textures/block/deepslate_bricks.png",
    "nightborn:gothic_stone_bricks, nightborn:textures/block/gothic_stone_bricks.png"
  })
  @DisplayName("a coffin is drawn with its source block's own texture")
  void drawsTheSourceBlockTexture(final String source, final String texture) {
    assertEquals(Identifier.of(texture), CoffinArt.texture(Identifier.of(source)));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "body_wood, nightborn:textures/block/source.png",
    "lid_wood, nightborn:textures/block/source.png",
    "body_stone, nightborn:textures/block/source.png",
    "lid_stone, nightborn:textures/block/source.png",
    "body_velvet, nightborn:textures/block/burgundy_velvet.png",
    "lid_velvet, nightborn:textures/block/burgundy_velvet.png",
    "body_gold, minecraft:textures/block/raw_gold_block.png",
    "lid_gold, minecraft:textures/block/raw_gold_block.png"
  })
  @DisplayName("each material bone binds its own texture")
  void bindsEachMaterialBone(final String bone, final String texture) {
    assertEquals(Optional.of(Identifier.of(texture)), CoffinArt.texture(bone, SOURCE));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"coffin", "body", "lid"})
  @DisplayName("a bone that only groups others keeps the buffer it was given")
  void leavesGroupingBonesAlone(final String bone) {
    assertEquals(Optional.empty(), CoffinArt.texture(bone, SOURCE));
  }

  static Stream<Arguments> styles() {
    return Stream.of(
        Arguments.of(
            CoffinStyle.WOOD,
            Set.of("body_wood", "body_velvet", "body_gold", "lid_wood", "lid_velvet", "lid_gold")),
        Arguments.of(CoffinStyle.STONE, Set.of("body_stone", "body_velvet", "lid_stone")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("styles")
  @DisplayName("every bone that carries cubes names a material the renderer can bind")
  void everyBoneWithCubesHasAMaterial(final CoffinStyle style, final Set<String> expected)
      throws IOException {
    final Set<String> bones =
        StreamSupport.stream(bones(style).spliterator(), false)
            .map(JsonElement::getAsJsonObject)
            .filter(bone -> bone.has("cubes"))
            .map(bone -> bone.get("name").getAsString())
            .collect(Collectors.toSet());
    assertEquals(expected, bones);
    bones.forEach(bone -> assertTrue(CoffinArt.texture(bone, SOURCE).isPresent(), bone));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(CoffinStyle.class)
  @DisplayName("the item is centred on each model's length as exported")
  void centresTheItemOnTheModel(final CoffinStyle style) throws IOException {
    double min = Double.MAX_VALUE;
    double max = -Double.MAX_VALUE;
    for (final JsonElement bone : bones(style)) {
      if (!bone.getAsJsonObject().has("cubes")) {
        continue;
      }
      for (final JsonElement cube : bone.getAsJsonObject().getAsJsonArray("cubes")) {
        final JsonArray origin = cube.getAsJsonObject().getAsJsonArray("origin");
        final JsonArray size = cube.getAsJsonObject().getAsJsonArray("size");
        final JsonArray pivot = cube.getAsJsonObject().getAsJsonArray("pivot");
        final double turn =
            cube.getAsJsonObject().has("rotation")
                ? Math.toRadians(
                    cube.getAsJsonObject().getAsJsonArray("rotation").get(1).getAsDouble())
                : 0.0;
        final double px = pivot == null ? 0.0 : pivot.get(0).getAsDouble();
        final double pz = pivot == null ? 0.0 : pivot.get(2).getAsDouble();
        for (int corner = 0; corner < 4; corner++) {
          final double x =
              origin.get(0).getAsDouble() + (corner & 1) * size.get(0).getAsDouble() - px;
          final double z =
              origin.get(2).getAsDouble() + (corner >> 1) * size.get(2).getAsDouble() - pz;
          final double turnedZ = pz - x * Math.sin(turn) + z * Math.cos(turn);
          min = Math.min(min, turnedZ);
          max = Math.max(max, turnedZ);
        }
      }
    }
    assertEquals((min + max) / 2.0 / 16.0, CoffinArt.MODEL_CENTRE_Z, HALF_PIXEL);
  }

  @Test
  @DisplayName("the velvet lining ships with the mod")
  void shipsTheVelvet() {
    assertTrue(
        Files.exists(
            Path.of("src/main/resources/assets", CoffinArt.VELVET.getNamespace())
                .resolve(CoffinArt.VELVET.getPath())));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(CoffinStyle.class)
  @DisplayName("each style's model and clips ship with the mod")
  void shipsEachStyle(final CoffinStyle style) {
    assertTrue(Files.isRegularFile(file(CoffinArt.geo(style))));
    assertTrue(Files.isRegularFile(file(CoffinArt.animation(style))));
  }

  private static JsonArray bones(final CoffinStyle style) throws IOException {
    return JsonParser.parseString(Files.readString(file(CoffinArt.geo(style))))
        .getAsJsonObject()
        .getAsJsonArray("minecraft:geometry")
        .get(0)
        .getAsJsonObject()
        .getAsJsonArray("bones");
  }

  private static Path file(final Identifier id) {
    return Path.of("src/client/resources/assets", id.getNamespace()).resolve(id.getPath());
  }
}
