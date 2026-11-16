package com.grahambartley.nightborn.tools.structures.vampire.castle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CurtainWallTest {

  @ParameterizedTest(name = "{0},{1}: wall {2}, outer {3}, walk {4}")
  @CsvSource({
    "0, 20, true, true, false",
    "1, 20, true, false, true",
    "2, 20, false, false, false",
    "20, 40, true, true, false",
    "20, 39, true, false, true",
    "20, 20, false, false, false"
  })
  void knowsItsRing(
      final int x, final int z, final boolean wall, final boolean outer, final boolean walk) {
    assertEquals(wall, CurtainWall.onWall(x, z));
    assertEquals(outer, CurtainWall.outerFace(x, z));
    assertEquals(walk, CurtainWall.onWalk(x, z));
  }

  @ParameterizedTest(name = "{0} at {1},{2} stands {3}")
  @CsvSource({
    "BASTIONS, 40, 20, true",
    "RUINED, 40, 20, false",
    "RUINED, 40, 5, true",
    "RUINED, 0, 20, true"
  })
  void onlyTheRuinedLayoutLosesItsTop(
      final String layout, final int x, final int z, final boolean whole) {
    final Castle.Layout chosen = layout.equals("RUINED") ? Castle.RUINED : Castle.BASTIONS;
    assertEquals(whole, CurtainWall.top(chosen, x, z) == Castle.WALL_TOP);
    assertTrue(CurtainWall.top(chosen, x, z) >= Castle.FLOOR);
  }

  @ParameterizedTest(name = "step {0} rises to {1}")
  @CsvSource({"0, 2", "4, 6", "8, 10"})
  void aStairClimbsToTheWalk(final int step, final int y) {
    final Template t = Castle.build(Castle.BASTIONS);
    final String state = t.get(CurtainWall.STAIR_X + step, y, CurtainWall.STAIR_Z);
    assertTrue(state.contains("stairs[facing=east"), state);
    assertEquals(BlockState.AIR, t.get(CurtainWall.STAIR_X + step, y + 1, CurtainWall.STAIR_Z));
  }

  @ParameterizedTest(name = "tower door {0}")
  @CsvSource({"0", "1", "2", "3"})
  void towersOpenOntoTheWalk(final int index) {
    final Template t = Castle.build(Castle.BASTIONS);
    final Tower tower = Castle.towers(Castle.BASTIONS).get(index);
    int doors = 0;
    for (int x = 0; x < Castle.SIZE; x++) {
      for (int z = 0; z < Castle.SIZE; z++) {
        if (tower.wall(x, z) && CurtainWall.onWalk(x, z)) {
          assertEquals(BlockState.AIR, t.get(x, Castle.WALL_TOP + 1, z));
          doors++;
        }
      }
    }
    assertTrue(doors >= 2, "doors " + doors);
  }
}
