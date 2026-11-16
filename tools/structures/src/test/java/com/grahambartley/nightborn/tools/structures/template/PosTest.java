package com.grahambartley.nightborn.tools.structures.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PosTest {

  @ParameterizedTest(name = "({0},{1},{2}) + ({3},{4},{5})")
  @CsvSource({"1, 1, 1, 1, 2, 3", "1, 1, 1, 0, 0, -1", "0, 0, 0, -4, 0, 9"})
  void offsetsByEachAxis(
      final int x, final int y, final int z, final int dx, final int dy, final int dz) {
    final Pos expected = new Pos(x + dx, y + dy, z + dz);
    assertEquals(expected, new Pos(x, y, z).offset(dx, dy, dz));
    assertEquals(expected, new Pos(x, y, z).offset(new Pos(dx, dy, dz)));
  }

  @ParameterizedTest(name = "({0},{1},{2}) saves before ({3},{4},{5})")
  @CsvSource({"0, 0, 0, 0, 0, 1", "0, 0, 1, 1, 0, 0", "1, 0, 0, 0, 1, 0", "9, 0, 9, 0, 1, 0"})
  void savesInYThenXThenZOrder(
      final int ax, final int ay, final int az, final int bx, final int by, final int bz) {
    assertTrue(Pos.SAVE_ORDER.compare(new Pos(ax, ay, az), new Pos(bx, by, bz)) < 0);
  }
}
