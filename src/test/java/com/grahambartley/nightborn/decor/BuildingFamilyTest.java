package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BuildingFamilyTest {

  static Stream<Arguments> families() {
    return Stream.of(
        Arguments.of(
            "gothic_stone_bricks",
            List.of(
                "gothic_stone_bricks",
                "gothic_stone_brick_stairs",
                "gothic_stone_brick_slab",
                "gothic_stone_brick_wall")),
        Arguments.of(
            "ebony_planks", List.of("ebony_planks", "ebony_stairs", "ebony_slab", "ebony_fence")),
        Arguments.of("carved_gothic_stone", List.of("carved_gothic_stone")),
        Arguments.of(
            "limewashed_bricks",
            List.of(
                "limewashed_bricks",
                "limewashed_brick_stairs",
                "limewashed_brick_slab",
                "limewashed_brick_wall")),
        Arguments.of(
            "worn_limewashed_bricks",
            List.of(
                "worn_limewashed_bricks",
                "worn_limewashed_brick_stairs",
                "worn_limewashed_brick_slab",
                "worn_limewashed_brick_wall")),
        Arguments.of(
            "clay_roof_tiles",
            List.of("clay_roof_tiles", "clay_roof_tile_stairs", "clay_roof_tile_slab")),
        Arguments.of("plaster", List.of("plaster")));
  }

  @ParameterizedTest(name = "{0} family lists {1}")
  @MethodSource("families")
  void listsItsMembersInFamilyOrder(final String baseId, final List<String> expected) {
    final BuildingFamily family =
        BuildingFamily.ALL.stream()
            .filter(f -> f.baseId().equals(baseId))
            .findFirst()
            .orElseThrow();
    assertEquals(expected, family.ids());
  }

  @Test
  void refusesAFamilyWithoutABaseBlock() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new BuildingFamily(
                "loose", BuildingFamily.Material.STONE, Set.of(FamilyMember.STAIRS)));
  }
}
