package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class IvyTest {

  @ParameterizedTest(name = "{0} climbable {1}")
  @CsvSource({
    "minecraft:bricks, true",
    "nightborn:gothic_stone_bricks, true",
    "nightborn:mossy_gothic_stone_bricks, true",
    "minecraft:glass_pane, false",
    "minecraft:dark_oak_planks, false",
    "minecraft:deepslate_tiles, false"
  })
  void climbsOnlyMasonry(final String state, final boolean climbs) {
    assertEquals(climbs, Ivy.climbs(state));
  }

  @ParameterizedTest(name = "a wall facing {0} gets vines held on its side")
  @CsvSource({"north, 3, 2, south", "south, 3, 4, north", "east, 4, 3, west", "west, 2, 3, east"})
  void vinesHoldTheWallBesideThem(
      final String wallSide, final int vx, final int vz, final String held) {
    final Template t = new Template("vampire/test", 7, Ivy.REACH + 2, 7);
    for (int y = 0; y < t.sizeY(); y++) {
      t.set(3, y, 3, "minecraft:bricks");
    }
    Ivy.grow(t);
    for (int y = Ivy.LOWEST; y <= Ivy.REACH; y++) {
      final String cell = t.get(vx, y, vz);
      if (cell != null) {
        assertTrue(cell.contains(held + "=true"), cell);
      }
    }
  }

  @ParameterizedTest(name = "ivy never replaces what is built at y {0}")
  @ValueSource(ints = {1, 5, 9})
  void neverReplacesBuiltCells(final int y) {
    final Template t = new Template("vampire/test", 24, Ivy.REACH + 2, 24);
    t.fill(10, 0, 0, 10, Ivy.REACH, 23, "minecraft:bricks");
    t.fill(11, 0, 0, 11, Ivy.REACH, 23, "minecraft:glass");
    Ivy.grow(t);
    for (int z = 0; z < 24; z++) {
      assertEquals("minecraft:glass", t.get(11, y, z));
    }
  }

  @ParameterizedTest(name = "patches cover some walls and not others")
  @ValueSource(ints = {48})
  void patchesAreRagged(final int span) {
    int covered = 0;
    for (int x = 0; x < span; x++) {
      for (int z = 0; z < span; z++) {
        if (Ivy.patch(x, z)) {
          covered++;
        }
      }
    }
    final double share = covered / (double) (span * span);
    assertTrue(share > 0.15 && share < 0.45, "share " + share);
  }
}
