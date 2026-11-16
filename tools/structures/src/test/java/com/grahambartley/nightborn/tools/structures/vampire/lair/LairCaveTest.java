package com.grahambartley.nightborn.tools.structures.vampire.lair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LairCaveTest {

  private static Template cave(final int sizeX, final int sizeY, final int sizeZ) {
    final Template t = new Template("vampire/test", sizeX, sizeY, sizeZ);
    LairCave.carve(t);
    return t;
  }

  private static boolean rock(final String state) {
    return state.contains("deepslate") || state.contains("tuff");
  }

  @Test
  void theCaveIsRockWithAnOpenMiddle() {
    final Template t = cave(15, 8, 15);
    assertTrue(LairCave.roof(t, 7, 7) > LairCave.WALL_HEIGHT);
    assertEquals(BlockState.AIR, t.get(7, 1, 7));
    assertTrue(rock(t.get(7, 0, 7)));
    assertTrue(rock(t.get(0, 2, 0)));
    assertTrue(rock(t.get(7, 7, 7)));
  }

  @ParameterizedTest(name = "the cave is walled in at its box edge in a {0} by {1} room")
  @CsvSource({"15, 15", "25, 25", "29, 15"})
  void theBoxEdgeIsAlwaysRock(final int sizeX, final int sizeZ) {
    final Template t = cave(sizeX, 12, sizeZ);
    for (int x = 0; x < sizeX; x++) {
      assertEquals(0, LairCave.roof(t, x, 0));
      assertEquals(0, LairCave.roof(t, x, sizeZ - 1));
    }
    for (int z = 0; z < sizeZ; z++) {
      assertEquals(0, LairCave.roof(t, 0, z));
      assertEquals(0, LairCave.roof(t, sizeX - 1, z));
    }
  }

  @ParameterizedTest(name = "the roof of a {0} by {1} cave is sealed by rock above")
  @CsvSource({"25, 25", "29, 15"})
  void theRoofNeverReachesTheTopOfTheBox(final int sizeX, final int sizeZ) {
    final Template t = cave(sizeX, 12, sizeZ);
    for (int x = 0; x < sizeX; x++) {
      for (int z = 0; z < sizeZ; z++) {
        assertTrue(rock(t.get(x, 11, z)), x + "," + z);
      }
    }
  }

  @ParameterizedTest(name = "every hanging dripstone in a {0} by {1} cave hangs from rock")
  @CsvSource({"25, 25", "29, 15"})
  void hangingDripstoneHangsFromRock(final int sizeX, final int sizeZ) {
    final Template t = cave(sizeX, 12, sizeZ);
    int hanging = 0;
    for (final var cell : t.cells().entrySet()) {
      if (cell.getValue().contains("vertical_direction=down")) {
        hanging++;
        final String above = t.get(cell.getKey().x(), cell.getKey().y() + 1, cell.getKey().z());
        assertTrue(!above.endsWith("air"), cell.getKey().toString());
      }
    }
    assertTrue(hanging > 0);
  }
}
