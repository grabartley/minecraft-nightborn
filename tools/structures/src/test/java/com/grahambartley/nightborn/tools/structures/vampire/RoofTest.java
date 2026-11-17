package com.grahambartley.nightborn.tools.structures.vampire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RoofTest {

  private static final Roof.Cover SLATE =
      new Roof.Cover("deepslate_tile_stairs", "deepslate_tiles", "deepslate_tile_slab");
  private static final Stone WALL = new Stone("stone_bricks", "stone_bricks", "stone_bricks", 0);

  @ParameterizedTest(name = "a {0} roof over walls {1} and {2} peaks at {3}")
  @CsvSource({"X, 1, 9, 10", "Z, 1, 9, 10", "X, 2, 6, 8"})
  void bothSlopesClimbToOneRidge(final Axis ridge, final int low, final int high, final int top) {
    final Template t = new Template("vampire/test", 12, 14, 12);
    assertEquals(top, Roof.gable(t, ridge, 3, 6, low, high, 4, SLATE, WALL));
    final int centre = (low + high) / 2;
    assertEquals(
        "minecraft:deepslate_tiles", t.get(ridge.x(4, centre), top - 1, ridge.z(4, centre)));
    assertTrue(
        t.get(ridge.x(4, centre), top, ridge.z(4, centre))
            .startsWith("minecraft:deepslate_tile_slab"));
    assertTrue(
        t.get(ridge.x(4, low), 5, ridge.z(4, low)).contains("facing=" + ridge.across().high()));
    assertTrue(
        t.get(ridge.x(4, high), 5, ridge.z(4, high)).contains("facing=" + ridge.across().low()));
  }

  @ParameterizedTest(name = "an overhang of {0} puts the first course {1} outside the wall")
  @CsvSource({"true, 0", "false, 1"})
  void overhangChoosesTheFirstCourse(final boolean overhang, final int lowest) {
    final Template t = new Template("vampire/test", 12, 14, 12);
    Roof.gable(t, Axis.X, 3, 6, 1, 9, 4, SLATE, WALL, overhang);
    assertEquals(overhang, t.get(4, 4, 0) != null);
    assertTrue(t.get(4, 4 + lowest, lowest).startsWith("minecraft:deepslate_tile_stairs"));
  }

  @ParameterizedTest(name = "gable ends fill the triangle under the roof at {0}")
  @CsvSource({"3", "6"})
  void gablesCloseTheEnds(final int end) {
    final Template t = new Template("vampire/test", 12, 14, 12);
    Roof.gable(t, Axis.X, 3, 6, 1, 9, 4, SLATE, WALL);
    assertEquals("minecraft:stone_bricks", t.get(end, 6, 5));
    assertEquals(null, t.get(4, 6, 5));
  }

  @ParameterizedTest(name = "ledged {0}: the second course is {1}")
  @CsvSource({"true, minecraft:deepslate_tiles", "false, minecraft:deepslate_tile_stairs"})
  void ledgedRoofsStepEveryOtherCourseSoSnowCanLie(final boolean ledged, final String second) {
    final Template t = new Template("vampire/test", 12, 14, 12);
    Roof.gable(t, Axis.X, 3, 6, 1, 9, 4, SLATE, WALL, true, ledged);
    assertTrue(t.get(4, 4, 0).startsWith("minecraft:deepslate_tile_stairs"));
    assertTrue(t.get(4, 5, 1).startsWith(second), t.get(4, 5, 1));
    assertTrue(t.get(4, 5, 9).startsWith(second), t.get(4, 5, 9));
    assertTrue(t.get(4, 6, 2).startsWith("minecraft:deepslate_tile_stairs"));
  }

  @ParameterizedTest(name = "an overhanging roof closes the wall top at {0},{1} under its eave")
  @CsvSource({"3, 1", "3, 5", "6, 9", "4, 1", "5, 9", "6, 5"})
  void overhangSealsTheCourseBetweenWallAndRoof(final int along, final int across) {
    final Template t = new Template("vampire/test", 12, 14, 12);
    Roof.gable(t, Axis.X, 3, 6, 1, 9, 4, SLATE, WALL);
    assertEquals("minecraft:stone_bricks", t.get(along, 4, across));
  }

  @ParameterizedTest(name = "sealing the eave keeps the {0} already on the wall top")
  @CsvSource({"minecraft:dark_oak_log", "minecraft:glass_pane"})
  void sealingKeepsWhatIsAlreadyThere(final String wallTop) {
    final Template t = new Template("vampire/test", 12, 14, 12);
    t.set(3, 4, 5, wallTop);
    Roof.gable(t, Axis.X, 3, 6, 1, 9, 4, SLATE, WALL);
    assertEquals(wallTop, t.get(3, 4, 5));
    assertNull(t.get(4, 4, 5));
  }
}
