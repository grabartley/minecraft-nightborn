package com.grahambartley.nightborn.tools.structures.template;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class PosTest {

  @Test
  void offsetsByEachAxis() {
    assertEquals(new Pos(2, 3, 4), new Pos(1, 1, 1).offset(1, 2, 3));
    assertEquals(new Pos(1, 1, 0), new Pos(1, 1, 1).offset(new Pos(0, 0, -1)));
  }

  @Test
  void savesInYThenXThenZOrder() {
    final List<Pos> sorted =
        List.of(new Pos(0, 1, 0), new Pos(1, 0, 0), new Pos(0, 0, 1), new Pos(0, 0, 0)).stream()
            .sorted(Pos.SAVE_ORDER)
            .toList();
    assertEquals(
        List.of(new Pos(0, 0, 0), new Pos(0, 0, 1), new Pos(1, 0, 0), new Pos(0, 1, 0)), sorted);
  }
}
