package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class EntityRigAssetsTest {

  private static final Pattern SIDE =
      Pattern.compile("(?:^|_)(right|left)(?:_|$)|^armor(Right|Left)");
  private static final double RIGHT =
      Math.signum(
          TexturedModelData.of(PlayerEntityModel.getTexturedModelData(Dilation.NONE, false), 64, 64)
              .createModel()
              .getChild(EntityModelPartNames.RIGHT_ARM)
              .pivotX);

  record Bone(String rig, String name, double side, double pivotX, List<Double> cubeCentres) {

    @Override
    public String toString() {
      return rig + " " + name;
    }
  }

  static Stream<Bone> sidedBones() throws IOException {
    final List<Bone> found = new ArrayList<>();
    for (final Path dir : List.of(GeoAssets.ENTITY_GEO, GeoAssets.ARMOR_GEO)) {
      for (final Path file : GeoAssets.geoFiles(dir)) {
        for (final JsonElement element : GeoAssets.bones(file)) {
          final JsonObject bone = element.getAsJsonObject();
          final String name = bone.get("name").getAsString();
          final Matcher match = SIDE.matcher(name);
          if (match.find() && bone.has("pivot")) {
            final String side = match.group(1) != null ? match.group(1) : match.group(2);
            found.add(
                new Bone(
                    file.getFileName().toString(),
                    name,
                    side.equalsIgnoreCase("right") ? RIGHT : -RIGHT,
                    bone.getAsJsonArray("pivot").get(0).getAsDouble(),
                    cubeCentres(bone)));
          }
        }
      }
    }
    return found.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("sidedBones")
  @DisplayName("every sided bone pivots on the side vanilla's player model puts that arm")
  void pivotsOnItsNamedSide(final Bone bone) {
    assertNotEquals(0.0, bone.pivotX(), "a sided bone is off the centre line");
    assertEquals(bone.side(), Math.signum(bone.pivotX()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("sidedBones")
  @DisplayName("every cube of a sided bone sits on that bone's side")
  void cubesSitOnItsNamedSide(final Bone bone) {
    for (final double centre : bone.cubeCentres()) {
      assertEquals(bone.side(), Math.signum(centre), "cube centred at x " + centre);
    }
  }

  private static List<Double> cubeCentres(final JsonObject bone) {
    final List<Double> centres = new ArrayList<>();
    if (bone.has("cubes")) {
      for (final JsonElement cube : bone.getAsJsonArray("cubes")) {
        final JsonArray origin = cube.getAsJsonObject().getAsJsonArray("origin");
        final JsonArray size = cube.getAsJsonObject().getAsJsonArray("size");
        centres.add(origin.get(0).getAsDouble() + size.get(0).getAsDouble() / 2.0);
      }
    }
    return centres;
  }
}
