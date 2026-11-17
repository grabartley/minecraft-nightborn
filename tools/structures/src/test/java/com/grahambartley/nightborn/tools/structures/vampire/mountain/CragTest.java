package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CragTest {

  private static Template box() {
    return new Template(
        "vampire/test", MountainCastle.SIZE, MountainCastle.HEIGHT, MountainCastle.SIZE);
  }

  @ParameterizedTest(name = "{0},{1} stands at {2}")
  @CsvSource({
    "31, 31, 32",
    "6, 4, 32",
    "56, 44, 32",
    "5, 20, -1",
    "31, 45, 26",
    "10, 58, 26",
    "9, 50, -1",
    "31, 59, -1",
    "57, 10, -1"
  })
  void theUpperWardSitsSixAboveTheLowerAndNothingLiesOutside(
      final int x, final int z, final int level) {
    assertEquals(level, Crag.level(x, z));
  }

  @ParameterizedTest(name = "{0},{1}")
  @CsvSource({"31, 31", "12, 50", "56, 4"})
  void fillsRockUnderAYardWithAirOverIt(final int x, final int z) {
    final Template t = box();
    Crag.build(t);
    final int level = Crag.level(x, z);
    for (int y = 0; y < level; y++) {
      assertEquals("minecraft:" + Crag.rock(x, y, z), t.get(x, y, z));
    }
    assertEquals("minecraft:" + Crag.yard(x, z), t.get(x, level, z));
    assertEquals(BlockState.AIR, t.get(x, level + 1, z));
    assertEquals(BlockState.AIR, t.get(x, MountainCastle.UP + Crag.HEADROOM, z));
    assertEquals(null, t.get(x, MountainCastle.UP + Crag.HEADROOM + 1, z));
  }

  @ParameterizedTest(name = "{0},{1},{2}")
  @CsvSource({"0, 0, 0", "31, 12, 31", "62, 25, 62", "17, 3, 40", "5, 31, 5"})
  void rockAndYardsAreBareStoneThatGrowsNothing(final int x, final int y, final int z) {
    for (final String block : new String[] {Crag.rock(x, y, z), Crag.yard(x, z)}) {
      assertTrue(
          block.equals("stone")
              || block.equals("andesite")
              || block.equals("cobblestone")
              || block.equals("mossy_cobblestone"),
          block);
    }
  }

  @ParameterizedTest(name = "a wall foot at {0},{1}")
  @CsvSource({"6, 20", "31, 4", "52, 50"})
  void wallFeetRunAFewCoursesIntoTheRock(final int x, final int z) {
    final Template t = box();
    final int level = Crag.level(x, z);
    Crag.column(t, x, z, level);
    final int footing = Crag.footing(x, z);
    assertTrue(footing >= 3 && footing <= 6, "footing " + footing);
    assertTrue(t.get(x, level, z).contains("stone_bricks"), t.get(x, level, z));
    assertTrue(t.get(x, level - footing, z).contains("stone_bricks"));
    assertEquals(
        "minecraft:" + Crag.rock(x, level - footing - 1, z), t.get(x, level - footing - 1, z));
    assertEquals("minecraft:" + Crag.rock(x, 0, z), t.get(x, 0, z));
  }

  @ParameterizedTest(name = "{0} blocks out from level {1}")
  @CsvSource({"1, 32", "2, 32", "3, 32", "1, 26", "3, 26"})
  void theSkirtFallsAwayFromTheWalls(final int distance, final int level) {
    final int top = Crag.skirtTop(level, distance, 4, 9);
    assertTrue(top <= level - 2 - 5 * distance && top > level - 7 - 5 * distance, "top " + top);
  }

  @ParameterizedTest(name = "{0},{1} gets a skirt: {2}")
  @CsvSource({
    "5, 20, true",
    "3, 20, true",
    "2, 20, false",
    "58, 30, true",
    "31, 61, true",
    "31, 62, false"
  })
  void theSkirtReachesThreeBlocksPastTheCastle(final int x, final int z, final boolean skirted) {
    final Template t = box();
    Crag.build(t);
    Crag.skirt(t);
    assertEquals(skirted, t.get(x, 0, z) != null);
    if (skirted) {
      assertEquals(null, t.get(x, MountainCastle.UP, z));
    }
  }
}
