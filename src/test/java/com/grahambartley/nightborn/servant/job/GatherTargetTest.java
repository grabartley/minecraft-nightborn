package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GatherTargetTest {

  @ParameterizedTest(name = "crop {0}, ripe {1}, wood {2}, tree {3}, ore {4}, pickaxe {5} -> {6}")
  @CsvSource({
    "true, true, false, false, false, false, CROP",
    "true, false, false, false, false, false, NONE",
    "false, false, true, true, false, false, WOOD",
    "false, false, true, false, false, false, NONE",
    "false, false, false, false, true, true, ORE",
    "false, false, false, false, true, false, NONE",
    "false, false, false, false, false, false, NONE"
  })
  @DisplayName(
      "a servant takes ripe crops, the logs of a tree, and ore only with a pickaxe that can mine it")
  void of(
      final boolean crop,
      final boolean ripe,
      final boolean wood,
      final boolean tree,
      final boolean ore,
      final boolean pickaxe,
      final GatherTarget target) {
    assertEquals(target, GatherTarget.of(crop, ripe, wood, tree, ore, pickaxe));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"CROP, false", "WOOD, false", "ORE, true", "NONE, false"})
  @DisplayName("only mining ore wears the pickaxe")
  void wears(final GatherTarget target, final boolean wears) {
    assertEquals(wears, target.wearsThePickaxe());
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"CROP, true", "WOOD, false", "ORE, false", "NONE, false"})
  @DisplayName("only a harvested crop is sown again")
  void replants(final GatherTarget target, final boolean replants) {
    assertEquals(replants, target.replants());
  }
}
