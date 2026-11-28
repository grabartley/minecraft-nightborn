package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WindowTest {

  @ParameterizedTest(name = "a window facing {0} has its reveal at {1},{2}")
  @CsvSource({"north, 5, 6", "south, 5, 4", "east, 4, 5", "west, 6, 5"})
  void revealsSitInsideTheGlass(final String outward, final int x, final int z) {
    assertEquals(new Pos(x, Window.GROUND_SILL, z), Window.ground(5, 5, outward, "d").reveal());
  }

  @ParameterizedTest(name = "a {0} window facing {1} is glazed, hooded and curtained")
  @CsvSource({"ground, north", "ground, east", "upper, south", "upper, west"})
  void cutsGlassHoodSillAndCurtain(final String floor, final String outward) {
    final Template t = new Template("vampire/test", 11, Manor.TOP + 2, 11);
    final Window window =
        floor.equals("ground")
            ? Window.ground(5, 5, outward, Manor.BURGUNDY)
            : Window.upper(5, 5, outward, Manor.BURGUNDY);
    window.cut(t);
    final Pos in = window.reveal();
    for (int y = window.sill(); y < window.head(); y++) {
      assertEquals("minecraft:" + Manor.PANE, t.get(5, y, 5));
      final String drape = t.get(in.x(), y, in.z());
      assertTrue(drape.startsWith(Manor.BURGUNDY) && drape.contains("open=false"), drape);
      assertEquals(y == window.head() - 1, drape.contains("top=true"), drape);
    }
    final Pos out = Facing.STEP.get(outward);
    assertTrue(t.get(5 + out.x(), window.head(), 5 + out.z()).contains("half=top"));
    assertTrue(t.get(5 + out.x(), window.sill() - 1, 5 + out.z()).contains("type=top"));
  }

  @ParameterizedTest(name = "rows({0}, {1}) heads at {2}")
  @CsvSource({"11, 6, 17", "14, 3, 17", "7, 4, 11"})
  void rowsSetTheOpening(final int sill, final int height, final int head) {
    assertEquals(head, Window.upper(1, 1, "north", "d").rows(sill, height).head());
  }
}
