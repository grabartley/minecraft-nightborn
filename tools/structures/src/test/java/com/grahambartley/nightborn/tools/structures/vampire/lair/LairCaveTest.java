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

  @ParameterizedTest(name = "a bare {0} by {1} cave keeps two blocks of rock round it")
  @CsvSource({"15, 15", "25, 25", "31, 15"})
  void theShellIsTwoBlocksThick(final int sizeX, final int sizeZ) {
    LairChecks.assertSealed(cave(sizeX, 12, sizeZ));
  }

  @ParameterizedTest(name = "every hanging dripstone in a {0} by {1} cave hangs from rock")
  @CsvSource({"25, 25", "31, 15"})
  void hangingDripstoneHangsFromRock(final int sizeX, final int sizeZ) {
    assertTrue(LairChecks.assertHangingWhole(cave(sizeX, 12, sizeZ)) > 0);
  }
}
