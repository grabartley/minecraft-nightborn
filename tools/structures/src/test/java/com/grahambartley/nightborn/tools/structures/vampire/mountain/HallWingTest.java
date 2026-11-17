package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HallWingTest {

  private static Template box() {
    return new Template(
        "vampire/test", MountainCastle.SIZE, MountainCastle.HEIGHT, MountainCastle.SIZE);
  }

  private static Template built() {
    final Template t = box();
    Crag.build(t);
    HallWing.build(t);
    return t;
  }

  @ParameterizedTest(name = "{0},{1}: wall {2}, post {3}")
  @CsvSource({
    "8, 14, true, true",
    "20, 36, true, true",
    "8, 17, true, true",
    "8, 18, true, false",
    "11, 14, true, true",
    "12, 36, true, false",
    "14, 25, false, false"
  })
  void postsStandAtEveryCornerAndEveryThirdBlock(
      final int x, final int z, final boolean wall, final boolean post) {
    assertEquals(wall, HallWing.onWall(x, z));
    if (wall) {
      assertEquals(post, HallWing.post(x, z));
    }
  }

  @ParameterizedTest(name = "{0},{1},{2} is {3}")
  @CsvSource({
    "8, 38, 20, dark_oak_log[axis=z]",
    "12, 43, 14, dark_oak_log[axis=x]",
    "8, 40, 17, dark_oak_log[axis=y]",
    "8, 40, 18, nightborn:plaster",
    "20, 40, 36, dark_oak_log[axis=y]"
  })
  void theUpperFloorIsTimberFramedOverPlaster(
      final int x, final int y, final int z, final String frame) {
    assertEquals(frame.contains(":") ? frame : "minecraft:" + frame, built().get(x, y, z));
    assertTrue(HallWing.frame(x, y, z).endsWith(frame.replace("nightborn:", "")));
  }

  @ParameterizedTest(name = "{0},{1},{2} is {3}")
  @CsvSource({
    "8, 35, 20, limewashed_bricks",
    "20, 33, 25, air",
    "20, 34, 26, air",
    "20, 35, 25, limewashed_bricks",
    "20, 34, 18, glass_pane",
    "20, 40, 18, glass_pane",
    "14, 38, 25, dark_oak_planks",
    "14, 32, 25, dark_oak_planks",
    "14, 44, 25,",
    "7, 44, 25, clay_roof_tile_stairs",
    "8, 45, 25, clay_roof_tiles"
  })
  void aStoneGroundFloorCarriesItUnderATiledRoof(
      final int x, final int y, final int z, final String block) {
    final String state = built().get(x, y, z);
    if (block == null) {
      assertEquals(null, state);
    } else {
      assertTrue(state.contains(block), state);
    }
  }

  @ParameterizedTest(name = "step {0}")
  @CsvSource({"0", "1", "2", "3", "4", "5"})
  void aFlightClimbsToTheLoft(final int step) {
    final Template t = built();
    final int x = HallWing.WEST + 2 + step;
    final int y = HallWing.GROUND + 1 + step;
    assertTrue(t.get(x, y, HallWing.NORTH + 1).contains("dark_oak_stairs[facing=east"));
    assertEquals(BlockState.AIR, t.get(x, y + 1, HallWing.NORTH + 1));
    assertEquals(BlockState.AIR, t.get(x, y + 2, HallWing.NORTH + 1));
  }

  @ParameterizedTest(name = "the {0} floor holds {1}")
  @CsvSource({
    "33, ornate_table",
    "33, ornate_chair",
    "34, chalice",
    "37, chandelier",
    "33, chest",
    "39, bookshelf",
    "39, lectern",
    "39, chest"
  })
  void aFeastHallLiesUnderALibrary(final int y, final String thing) {
    assertTrue(
        built().cells().entrySet().stream()
            .anyMatch(cell -> cell.getKey().y() == y && cell.getValue().contains(thing)),
        thing);
  }
}
