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
import org.junit.jupiter.params.provider.MethodSource;

class HunterKitTest {

  private static final Path KITS =
      Path.of("src/main/resources/data/nightborn/nightborn/hunter_kit");

  private static Identifier id(final String path) {
    return Identifier.of("nightborn", path);
  }

  private static HunterKit kit(final boolean bolt, final int weight) {
    return new HunterKit(
        id("weapon"),
        bolt ? Optional.of(id("bolt")) : Optional.empty(),
        Optional.empty(),
        false,
        weight,
        0.0F);
  }

  private static final Map<Identifier, HunterKit> SAMPLE =
      Map.of(id("a_stake"), kit(false, 3), id("b_crossbow"), kit(true, 1));

  static Stream<Map.Entry<Identifier, HunterKit>> shippedKits() throws IOException {
    final Map<Identifier, HunterKit> kits = new TreeMap<>();
    try (Stream<Path> files = Files.list(KITS)) {
      for (final Path file : files.toList()) {
        kits.put(
            id(file.getFileName().toString().replace(".json", "")),
            HunterKit.CODEC
                .parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file)))
                .getOrThrow());
      }
    }
    return kits.entrySet().stream();
  }

  @ParameterizedTest(name = "roll {0}: {1}")
  @CsvSource({"0.0, a_stake", "0.74, a_stake", "0.76, b_crossbow", "0.99, b_crossbow"})
  void picksAKitByWeight(final double roll, final String picked) {
    assertEquals(Optional.of(id(picked)), HunterKit.pick(SAMPLE, roll));
  }

  @ParameterizedTest(name = "roll {0}")
  @CsvSource({"0.0", "0.5", "0.99"})
  void neverPicksAKitWeightedNothingForAnOrdinaryHunter(final double roll) {
    assertEquals(
        Optional.of(id("b_crossbow")),
        HunterKit.pick(Map.of(id("a_raid"), kit(false, 0), id("b_crossbow"), kit(true, 1)), roll));
  }

  @ParameterizedTest(name = "roll {0}")
  @CsvSource({"0.0", "0.99"})
  void picksNothingWhenThereAreNoKits(final double roll) {
    assertEquals(Optional.empty(), HunterKit.pick(Map.of(), roll));
  }

  @ParameterizedTest(name = "{0}: damage {1}")
  @CsvSource({
    "'{\"weapon\": \"nightborn:wooden_stake\"}', 0.0",
    "'{\"weapon\": \"nightborn:wooden_stake\", \"damage\": 6.0}', 6.0"
  })
  void readsItsOwnDamageOrNone(final String json, final float damage) {
    assertEquals(
        damage,
        HunterKit.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .getOrThrow()
            .damage());
  }

  @ParameterizedTest(name = "bolt {0}: ranged {0}")
  @CsvSource({"true", "false"})
  void isRangedOnlyWithABolt(final boolean bolt) {
    assertEquals(bolt, kit(bolt, 1).ranged());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("shippedKits")
  void everyShippedKitFinishesAVampireWithAStake(final Map.Entry<Identifier, HunterKit> kit) {
    final HunterKit shipped = kit.getValue();
    final Identifier finisher = shipped.bolt().orElse(shipped.weapon());
    assertTrue(finisher.getPath().contains("stake"), kit.getKey() + " ends with " + finisher);
  }
}
