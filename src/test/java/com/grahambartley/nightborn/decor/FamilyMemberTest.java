package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FamilyMemberTest {

  @ParameterizedTest(name = "{0} of {1} is {2}")
  @CsvSource({
    "BASE, gothic_stone_bricks, gothic_stone_bricks",
    "STAIRS, gothic_stone_bricks, gothic_stone_brick_stairs",
    "SLAB, cracked_gothic_stone_bricks, cracked_gothic_stone_brick_slab",
    "WALL, mossy_gothic_stone_bricks, mossy_gothic_stone_brick_wall",
    "STAIRS, limewashed_bricks, limewashed_brick_stairs",
    "WALL, worn_limewashed_bricks, worn_limewashed_brick_wall",
    "STAIRS, clay_roof_tiles, clay_roof_tile_stairs",
    "SLAB, clay_roof_tiles, clay_roof_tile_slab",
    "STAIRS, ebony_planks, ebony_stairs",
    "SLAB, ebony_planks, ebony_slab",
    "FENCE, ebony_planks, ebony_fence",
    "WALL, carved_gothic_stone, carved_gothic_stone_wall"
  })
  void namesEachMemberTheWayVanillaNamesItsFamilies(
      final FamilyMember member, final String baseId, final String expected) {
    assertEquals(expected, member.idFor(baseId));
  }
}
