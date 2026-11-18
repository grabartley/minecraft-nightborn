package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SpireTest {

  @ParameterizedTest(name = "round {0}: {1},{2} within {3} is {4}")
  @CsvSource({
    "true, 4, 0, 4, true",
    "true, 3, 3, 4, true",
    "true, 4, 3, 4, false",
    "true, 0, 0, 0, true",
    "true, 0, 0, -1, false",
    "false, 4, 4, 4, true",
    "false, 5, 0, 4, false",
    "false, -4, 3, 4, true"
  })
  void shapesRoundAndSquareFootprints(
      final boolean round, final int dx, final int dz, final int radius, final boolean within) {
    assertEquals(within, Spire.within(dx, dz, radius, round));
  }

  @ParameterizedTest(name = "round {0}: the tile at {1},{2} of ring {3} is {4}")
  @CsvSource({
    "false, 0, 3, 3, 'facing=north,half=bottom,shape=straight'",
    "false, 0, -3, 3, 'facing=south,half=bottom,shape=straight'",
    "false, 3, 1, 3, 'facing=west,half=bottom,shape=straight'",
    "false, -3, 1, 3, 'facing=east,half=bottom,shape=straight'",
    "false, -3, 3, 3, 'facing=north,half=bottom,shape=outer_right'",
    "false, 3, 3, 3, 'facing=north,half=bottom,shape=outer_left'",
    "false, -3, -3, 3, 'facing=south,half=bottom,shape=outer_left'",
    "false, 3, -3, 3, 'facing=south,half=bottom,shape=outer_right'",
    "true, 4, 1, 4, 'facing=west,half=bottom,shape=straight'",
    "true, -1, 4, 4, 'facing=north,half=bottom,shape=straight'",
    "true, -3, -3, 4, 'facing=east,half=bottom,shape=straight'"
  })
  void everyTileClimbsTowardsThePeak(
      final boolean round, final int dx, final int dz, final int radius, final String state) {
    final String tile = Spire.tile(dx, dz, radius, round);
    assertTrue(tile.startsWith("nightborn:clay_roof_tile_stairs["), tile);
    assertTrue(tile.contains(state), tile);
  }

  @ParameterizedTest(name = "ring {0}, layer {1} of {2}: sloped {3}")
  @CsvSource({
    "5, 0, 2, true",
    "5, 1, 2, false",
    "4, 0, 1, true",
    "3, 0, 1, false",
    "2, 0, 1, true"
  })
  void everyRingLeavesALedgeForSnow(
      final int radius, final int layer, final int layers, final boolean sloped) {
    assertEquals(sloped, Spire.sloped(radius, layer, layers));
  }

  @ParameterizedTest(name = "round {0}, radius {1}, {2} layers")
  @CsvSource({"true, 5, 2", "false, 5, 2", "false, 9, 1", "true, 1, 2"})
  void risesRingByRingOverASolidCoreToACappedFinial(
      final boolean round, final int radius, final int layers) {
    final Template t = new Template("vampire/test", 25, 30, 25);
    final int top = Spire.raise(t, 12, 12, radius, 3, round, layers);
    assertEquals(3 + Spire.height(radius, layers) - 1, top);
    assertTrue(t.get(12, top, 12).startsWith("minecraft:" + Spire.FINIAL), t.get(12, top, 12));
    assertEquals("nightborn:clay_roof_tiles", t.get(12, top - 1, 12));
    assertTrue(t.get(12 + radius, 3, 12).startsWith("nightborn:clay_roof_tile"));
    assertEquals(null, t.get(12 + radius + 1, 3, 12));
    assertEquals("nightborn:clay_roof_tiles", t.get(12, 3, 12));
  }
}
