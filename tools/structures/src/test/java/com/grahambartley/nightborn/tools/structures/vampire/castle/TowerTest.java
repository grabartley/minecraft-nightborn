package com.grahambartley.nightborn.tools.structures.vampire.castle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class TowerTest {

  @ParameterizedTest(name = "round {0}: {1},{2} inside {3}, wall {4}")
  @CsvSource({
    "true, 10, 10, true, false",
    "true, 14, 10, true, true",
    "true, 13, 14, false, false",
    "true, 13, 12, true, true",
    "false, 14, 14, true, true",
    "false, 13, 12, true, false",
    "false, 15, 10, false, false"
  })
  void shapesItsFootprint(
      final boolean round, final int x, final int z, final boolean inside, final boolean wall) {
    final Tower tower = new Tower(10, 10, 4, 17, round);
    assertEquals(inside, tower.inside(x, z, 4));
    assertEquals(wall, tower.wall(x, z));
  }

  @ParameterizedTest(name = "round {0}")
  @ValueSource(booleans = {true, false})
  void standsHollowWithASolidFootAndACappedRoof(final boolean round) {
    final Template t = new Template("vampire/test", 21, 40, 21);
    final Tower tower = new Tower(10, 10, 4, 17, round);
    tower.build(t, Castle.WALL, 10);
    assertTrue(t.get(10, 0, 10).contains("blackstone"), t.get(10, 0, 10));
    assertEquals(BlockState.AIR, t.get(10, Castle.FLOOR, 10));
    assertEquals("minecraft:" + Castle.PLANKS, t.get(10, 10, 10));
    assertTrue(t.get(10, tower.roofTop(), 10).startsWith("minecraft:" + Tower.FINIAL));
    assertTrue(t.get(14, 5, 10).startsWith("minecraft:iron_bars"), t.get(14, 5, 10));
  }

  @ParameterizedTest(name = "a door at {0},{1} opens two blocks of the wall")
  @CsvSource({"14, 10", "10, 6"})
  void doorsOpenTheWall(final int x, final int z) {
    final Template t = new Template("vampire/test", 21, 40, 21);
    final Tower tower = new Tower(10, 10, 4, 17, true);
    tower.build(t, Castle.WALL);
    tower.door(t, x, z);
    assertEquals(BlockState.AIR, t.get(x, Castle.FLOOR, z));
    assertEquals(BlockState.AIR, t.get(x, Castle.FLOOR + 1, z));
  }
}
