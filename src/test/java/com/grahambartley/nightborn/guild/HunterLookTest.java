package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
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
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class HunterLookTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets");
  private static final Path LOOKS =
      Path.of("src/main/resources/data/nightborn/nightborn/hunter_look");

  private static Identifier id(final String path) {
    return Identifier.of("nightborn", path);
  }

  private static HunterLook look(final HunterRig rig, final String texture, final int weight) {
    return new HunterLook(rig, id("textures/" + texture + ".png"), weight);
  }

  private static final Map<Identifier, HunterLook> SAMPLE =
      Map.of(
          id("a_man"), look(HunterRig.MALE, "a_man", 1),
          id("b_man"), look(HunterRig.MALE, "b_man", 3),
          id("a_woman"), look(HunterRig.FEMALE, "a_woman", 1));

  static Map<Identifier, HunterLook> shipped() throws IOException {
    final Map<Identifier, HunterLook> looks = new TreeMap<>();
    try (Stream<Path> files = Files.list(LOOKS)) {
      for (final Path file : files.toList()) {
        looks.put(
            id(file.getFileName().toString().replace(".json", "")),
            HunterLook.CODEC
                .parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file)))
                .getOrThrow());
      }
    }
    return looks;
  }

  static Stream<Map.Entry<Identifier, HunterLook>> shippedLooks() throws IOException {
    return shipped().entrySet().stream();
  }

  @ParameterizedTest(name = "{0} roll {1}: {2}")
  @CsvSource({
    "MALE, 0.0, a_man",
    "MALE, 0.24, a_man",
    "MALE, 0.26, b_man",
    "MALE, 0.99, b_man",
    "FEMALE, 0.0, a_woman",
    "FEMALE, 0.99, a_woman"
  })
  void picksALookOfTheRigByWeight(final HunterRig rig, final double roll, final String picked) {
    assertEquals(Optional.of(id(picked)), HunterLook.pick(SAMPLE, rig, roll));
  }

  @ParameterizedTest(name = "roll {0}")
  @CsvSource({"0.0", "0.5", "0.99"})
  void picksNothingForARigWithNoLook(final double roll) {
    assertEquals(Optional.empty(), HunterLook.pick(SAMPLE, HunterRig.GUILD_MASTER, roll));
  }

  @ParameterizedTest(name = "{0} wearing {1}: {2}")
  @CsvSource({
    "MALE, nightborn:b_man, nightborn:textures/b_man.png",
    "MALE, nightborn:a_woman, nightborn:textures/a_man.png",
    "MALE, nightborn:gone, nightborn:textures/a_man.png",
    "MALE, '', nightborn:textures/a_man.png",
    "FEMALE, nightborn:a_woman, nightborn:textures/a_woman.png",
    "GUILD_MASTER, nightborn:a_man, minecraft:missingno"
  })
  void drawsTheWornLookOrFallsBackToOneOfTheRig(
      final HunterRig rig, final String worn, final String texture) {
    assertEquals(
        Identifier.of(texture),
        HunterLook.texture(SAMPLE, Optional.ofNullable(Identifier.tryParse(worn)), rig));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(HunterRig.class)
  void everyRigShipsMoreThanOneLook(final HunterRig rig) throws IOException {
    assertTrue(shipped().values().stream().filter(look -> look.rig() == rig).count() > 1);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("shippedLooks")
  void everyShippedLookShipsItsTexture(final Map.Entry<Identifier, HunterLook> look) {
    final Identifier texture = look.getValue().texture();
    assertTrue(Files.exists(ASSETS.resolve(texture.getNamespace()).resolve(texture.getPath())));
  }
}
