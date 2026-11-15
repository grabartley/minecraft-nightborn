package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AmmoModelsTest {

  private static final Path MODELS = Path.of("src/main/resources/assets/nightborn/models");
  private static final List<Identifier> AMMO =
      Stream.of("stake_bolt", "silver_bolt", "holy_water_bolt", "garlic_bomb")
          .map(path -> Identifier.of("nightborn", path))
          .toList();

  static Stream<Arguments> stacks() {
    return Stream.of(
        Arguments.of("minecraft:potion", true, List.of(), "nightborn:item/holy_water"),
        Arguments.of(
            "minecraft:splash_potion", true, List.of(), "nightborn:item/splash_holy_water"),
        Arguments.of(
            "minecraft:lingering_potion", true, List.of(), "nightborn:item/lingering_holy_water"),
        Arguments.of("minecraft:potion", false, List.of(), null),
        Arguments.of("minecraft:tipped_arrow", true, List.of(), null),
        Arguments.of(
            "minecraft:crossbow",
            false,
            List.of("nightborn:stake_bolt"),
            "nightborn:item/crossbow_stake_bolt"),
        Arguments.of(
            "minecraft:crossbow",
            false,
            List.of("nightborn:silver_bolt"),
            "nightborn:item/crossbow_silver_bolt"),
        Arguments.of(
            "minecraft:crossbow",
            false,
            List.of("nightborn:holy_water_bolt"),
            "nightborn:item/crossbow_holy_water_bolt"),
        Arguments.of(
            "minecraft:crossbow",
            false,
            List.of("nightborn:garlic_bomb"),
            "nightborn:item/crossbow_garlic_bomb"),
        Arguments.of("minecraft:crossbow", false, List.of("minecraft:arrow"), null),
        Arguments.of("minecraft:crossbow", false, List.of(), null),
        Arguments.of("nightborn:stake_bolt", false, List.of(), null));
  }

  @ParameterizedTest(name = "{0} holy {1} loaded {2}")
  @MethodSource("stacks")
  @DisplayName("holy water and loaded crossbows draw their own models, all else stays vanilla")
  void modelFor(
      final String item, final boolean holy, final List<String> loaded, final String model) {
    assertEquals(
        Optional.ofNullable(model).map(Identifier::of),
        AmmoModels.modelFor(
            Identifier.of(item), holy, loaded.stream().map(Identifier::of).toList(), AMMO));
  }

  static Stream<Identifier> models() {
    return AmmoModels.all(AMMO).stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("models")
  @DisplayName("every swapped model ships in the resources")
  void shipped(final Identifier model) {
    assertTrue(Files.exists(MODELS.resolve(model.getPath() + ".json")));
  }
}
