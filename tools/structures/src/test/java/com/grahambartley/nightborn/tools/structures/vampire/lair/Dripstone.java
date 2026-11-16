package com.grahambartley.nightborn.tools.structures.vampire.lair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;

final class Dripstone {

  private Dripstone() {}

  static int assertHangingWhole(final Template t) {
    int hanging = 0;
    for (final var cell : t.cells().entrySet()) {
      final String state = cell.getValue();
      if (!state.contains("vertical_direction=down")) {
        continue;
      }
      hanging++;
      final int x = cell.getKey().x();
      final int y = cell.getKey().y();
      final int z = cell.getKey().z();
      final String above = t.get(x, y + 1, z);
      final String below = t.get(x, y - 1, z);
      assertTrue(above != null && !above.equals(BlockState.AIR), "hangs from nothing at " + cell);
      if (state.contains("thickness=frustum")) {
        assertTrue(below != null && below.contains("thickness=tip"), "no tip under " + cell);
      } else {
        assertEquals(BlockState.AIR, below, "tip buried at " + cell);
      }
    }
    return hanging;
  }
}
