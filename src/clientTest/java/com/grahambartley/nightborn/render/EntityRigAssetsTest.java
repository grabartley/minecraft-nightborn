package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class EntityRigAssetsTest {

  private static final Path ROOT = Path.of("src/client/resources/assets/nightborn/geo");
  private static final Pattern SIDE = Pattern.compile("(?:^|_)(right|left)(?:_|$)");
  private static final Pattern ARMOR_SIDE = Pattern.compile("^armor(Right|Left)");

  record Bone(String rig, String name, String side, double pivotX) {

    @Override
    public String toString() {
      return rig + " " + name;
    }
  }

  static Stream<Bone> entityBones() throws IOException {
    return bones(ROOT.resolve("entity"), SIDE).stream();
  }

  static Stream<Bone> armourBones() throws IOException {
    return bones(ROOT.resolve("armor"), ARMOR_SIDE).stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("entityBones")
  @DisplayName("every sided entity bone sits on the side the clothing armour bones use")
  void sitsOnItsNamedSide(final Bone bone) throws IOException {
    assertNotEquals(0.0, bone.pivotX(), "a sided bone is off the centre line");
    assertEquals(
        rightSign(), Math.signum(bone.pivotX()) * sign(bone.side()), "named side matches X");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("armourBones")
  @DisplayName("every clothing armour bone agrees on which X is right")
  void armourAgreesOnRight(final Bone bone) throws IOException {
    assertEquals(rightSign(), Math.signum(bone.pivotX()) * sign(bone.side()));
  }

  private static double rightSign() throws IOException {
    final List<Bone> armour = bones(ROOT.resolve("armor"), ARMOR_SIDE);
    assertFalse(armour.isEmpty(), "clothing armour rigs exist to read the convention from");
    final Bone first = armour.getFirst();
    return Math.signum(first.pivotX()) * sign(first.side());
  }

  private static double sign(final String side) {
    return side.equalsIgnoreCase("right") ? 1.0 : -1.0;
  }

  private static List<Bone> bones(final Path dir, final Pattern sided) throws IOException {
    final List<Bone> found = new ArrayList<>();
    try (Stream<Path> files = Files.list(dir)) {
      for (final Path file : files.filter(f -> f.toString().endsWith(".geo.json")).toList()) {
        final JsonObject geometry =
            JsonParser.parseString(Files.readString(file))
                .getAsJsonObject()
                .getAsJsonArray("minecraft:geometry")
                .get(0)
                .getAsJsonObject();
        for (final JsonElement element : geometry.getAsJsonArray("bones")) {
          final JsonObject bone = element.getAsJsonObject();
          final String name = bone.get("name").getAsString();
          final Matcher match = sided.matcher(name);
          if (match.find() && bone.has("pivot")) {
            found.add(
                new Bone(
                    file.getFileName().toString(),
                    name,
                    match.group(1),
                    bone.getAsJsonArray("pivot").get(0).getAsDouble()));
          }
        }
      }
    }
    return found;
  }
}
