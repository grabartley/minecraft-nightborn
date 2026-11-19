package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.mob.look.VampireRig;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class RestlessLookTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets");
  private static final Path LOOKS =
      Path.of("src/main/resources/data/nightborn/nightborn/restless_dead_look");

  private static Identifier id(final String path) {
    return Identifier.of("nightborn", path);
  }

  private static final Map<Identifier, RestlessLook> SAMPLE =
      Map.of(
          id("a_man"), new RestlessLook(VampireRig.MALE, id("textures/a_man.png"), 1),
          id("b_woman"), new RestlessLook(VampireRig.FEMALE, id("textures/b_woman.png"), 3));

  static Map<Identifier, RestlessLook> shipped() throws IOException {
    final Map<Identifier, RestlessLook> looks = new TreeMap<>();
    try (Stream<Path> files = Files.list(LOOKS)) {
      for (final Path file : files.toList()) {
        looks.put(
            id(file.getFileName().toString().replace(".json", "")),
            RestlessLook.CODEC
                .parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file)))
                .getOrThrow());
      }
    }
    return looks;
  }

  static Stream<String> shippedIds() throws IOException {
    return shipped().keySet().stream().map(Identifier::getPath);
  }

  @ParameterizedTest(name = "roll {0}: {1}")
  @CsvSource({"0.0, a_man", "0.24, a_man", "0.25, b_woman", "0.99, b_woman"})
  void picksByWeight(final double roll, final String expected) {
    assertEquals(Optional.of(id(expected)), RestlessLook.pick(SAMPLE, roll));
  }

  @ParameterizedTest(name = "worn {0}: {1}")
  @CsvSource({"nightborn:b_woman, b_woman", "nightborn:gone, a_man", "'', a_man"})
  void wornFallsBackToTheFirstLook(final String worn, final String expected) {
    assertEquals(SAMPLE.get(id(expected)), RestlessLook.worn(SAMPLE, worn).orElseThrow());
  }

  @ParameterizedTest(name = "rig {0} is refused")
  @CsvSource({"lord", "eldest_male"})
  void refusesBossRigs(final String rig) {
    final String json = "{\"rig\": \"" + rig + "\", \"texture\": \"nightborn:textures/x.png\"}";
    assertTrue(RestlessLook.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("shippedIds")
  void shippedLookPointsAtARealTexture(final String look) throws IOException {
    final Identifier texture = shipped().get(id(look)).texture();
    assertTrue(
        Files.exists(ASSETS.resolve(texture.getNamespace()).resolve(texture.getPath())),
        texture.toString());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"male", "female"})
  void shippedLooksDressBothCuts(final String rig) throws IOException {
    assertTrue(
        shipped().values().stream().anyMatch(look -> look.rig().asString().equals(rig)), rig);
  }
}
