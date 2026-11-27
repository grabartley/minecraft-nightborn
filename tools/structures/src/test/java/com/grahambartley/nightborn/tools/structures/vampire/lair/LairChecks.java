package com.grahambartley.nightborn.tools.structures.vampire.lair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.List;

final class LairChecks {

  private LairChecks() {}

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

  static void assertSealed(final Template t) {
    assertSealed(t, List.of());
  }

  static void assertSealed(final Template t, final List<Pos> passage) {
    for (int x = 0; x < t.sizeX(); x++) {
      for (int y = 0; y < t.sizeY(); y++) {
        for (int z = 0; z < t.sizeZ(); z++) {
          final boolean shell =
              x < LairCave.SHELL
                  || z < LairCave.SHELL
                  || x >= t.sizeX() - LairCave.SHELL
                  || z >= t.sizeZ() - LairCave.SHELL
                  || y >= t.sizeY() - LairCave.SHELL;
          final boolean dug = y <= LairPassage.HEADROOM && passage.contains(new Pos(x, 0, z));
          if (shell && !dug) {
            final String state = t.get(x, y, z);
            assertTrue(
                state != null && (state.contains("deepslate") || state.contains("tuff")),
                x + "," + y + "," + z + " is " + state);
          }
        }
      }
    }
  }
}
