package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GatherTargetTest {

  @ParameterizedTest(name = "crop {0}, ripe {1}, wood {2}, tree {3}, ore {4} -> {5}")
  @CsvSource({
    "true, true, false, false, false, CROP",
    "true, false, false, false, false, NONE",
    "false, false, true, true, false, WOOD",
    "false, false, true, false, false, NONE",
    "false, false, false, false, true, ORE",
    "false, false, false, false, false, NONE"
  })
  @DisplayName("a servant takes ripe crops, the logs of a tree and ore, never planks a player laid")
  void of(
      final boolean crop,
      final boolean ripe,
      final boolean wood,
      final boolean tree,
      final boolean ore,
      final GatherTarget target) {
    assertEquals(target, GatherTarget.of(crop, ripe, wood, tree, ore));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"CROP, true", "WOOD, false", "ORE, false", "NONE, false"})
  @DisplayName("only a harvested crop is sown again")
  void replants(final GatherTarget target, final boolean replants) {
    assertEquals(replants, target.replants());
  }
}
