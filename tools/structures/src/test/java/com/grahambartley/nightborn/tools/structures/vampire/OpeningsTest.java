package com.grahambartley.nightborn.tools.structures.vampire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OpeningsTest {

  @ParameterizedTest(name = "a three wide arch in a {0} wall is pointed")
  @CsvSource({"X", "Z"})
  void wideArchesGetShoulders(final Axis wall) {
    final Template t = new Template("vampire/test", 8, 6, 8);
    Openings.arch(t, wall, 2, 4, 1, 1, 3, "stone_brick_stairs");
    assertTrue(t.get(wall.x(2, 1), 3, wall.z(2, 1)).contains("facing=" + wall.low() + ",half=top"));
    assertTrue(
        t.get(wall.x(4, 1), 3, wall.z(4, 1)).contains("facing=" + wall.high() + ",half=top"));
    assertEquals(BlockState.AIR, t.get(wall.x(3, 1), 3, wall.z(3, 1)));
    assertEquals(BlockState.AIR, t.get(wall.x(2, 1), 1, wall.z(2, 1)));
  }

  @Test
  void aNarrowArchIsAPlainOpening() {
    final Template t = new Template("vampire/test", 8, 6, 8);
    Openings.arch(t, Axis.X, 3, 3, 1, 1, 2, "stone_brick_stairs");
    assertEquals(BlockState.AIR, t.get(3, 2, 1));
    assertEquals(null, t.get(2, 2, 1));
  }

  @Test
  void aLancetIsAColumnOfGlass() {
    final Template t = new Template("vampire/test", 8, 6, 8);
    Openings.lancet(t, Axis.Z, 4, 2, 1, 3, "glass_pane");
    for (int y = 1; y <= 3; y++) {
      assertEquals("minecraft:glass_pane", t.get(2, y, 4));
    }
    assertEquals(null, t.get(2, 4, 4));
  }
}
