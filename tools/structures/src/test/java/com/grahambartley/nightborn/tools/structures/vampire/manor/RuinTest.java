package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RuinTest {

  static Template t;

  @BeforeAll
  static void build() {
    t = new Template("vampire/test", Manor.SIZE_X, Manor.SIZE_Y, Manor.SIZE_Z);
    ManorShell.build(t);
    ManorRoofs.build(t);
    Ruin.fallenRoof(t);
  }

  @ParameterizedTest(name = "{0},{1} under the fallen roof {2}")
  @CsvSource({"50, 19, true", "51, 20, true", "55, 27, false", "46, 24, false", "53, 25, false"})
  void theHoleIsRoughlyRound(final int x, final int z, final boolean fallen) {
    assertEquals(fallen, Ruin.fallen(x, z));
  }

  @ParameterizedTest(name = "the sky shows through at {0},{1}")
  @CsvSource({"50, 19", "51, 20", "50, 18"})
  void theSkyShowsThroughTheHole(final int x, final int z) {
    for (int y = Manor.TOP; y < t.sizeY(); y++) {
      final String state = t.get(x, y, z);
      assertTrue(
          BlockState.AIR.equals(state) || x == Ruin.RAFTER_X, x + "," + y + "," + z + " " + state);
    }
  }

  @Test
  void theRestOfTheRoofStands() {
    assertEquals(Manor.PLANKS, t.get(55, Manor.TOP, 27));
    assertEquals(Manor.PLANKS, t.get(30, Manor.TOP, 22));
  }

  @Test
  void rubbleAndABrokenRafterLieWhereTheRoofFell() {
    int rubble = 0;
    for (int x = Ruin.FOOTPRINT.x0(); x <= Ruin.FOOTPRINT.x1(); x++) {
      for (int z = Ruin.FOOTPRINT.z0(); z <= Ruin.FOOTPRINT.z1(); z++) {
        if (!BlockState.AIR.equals(t.get(x, Manor.F1 + 1, z))) {
          assertTrue(Ruin.fallen(x, z), "rubble outside the hole at " + x + "," + z);
          rubble++;
        }
      }
    }
    assertTrue(rubble >= 6, "rubble " + rubble);
    assertTrue(t.get(Ruin.RAFTER_X, Manor.TOP + 2, 21).contains("dark_oak_log"));
  }
}
