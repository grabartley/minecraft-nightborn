package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StairHallTest {

  static Template t;

  @BeforeAll
  static void build() {
    t = new Template("vampire/test", Manor.SIZE_X, Manor.SIZE_Y, Manor.SIZE_Z);
    ManorShell.build(t);
    StairHall.build(t);
  }

  @ParameterizedTest(name = "{0},{1} over the open hall {2}")
  @CsvSource({
    "38, 30, true",
    "36, 23, true",
    "40, 37, true",
    "35, 30, false",
    "38, 38, false",
    "38, 22, false"
  })
  void theHallIsOpenToTheRoofBetweenItsGalleries(final int x, final int z, final boolean open) {
    assertEquals(open, StairHall.overVoid(x, z));
    if (open) {
      for (int y = Manor.F0 + 1; y < Manor.TOP; y++) {
        assertTrue(!t.get(x, y, z).contains("planks"), x + "," + y + "," + z);
      }
    }
  }

  @ParameterizedTest(name = "the main flight at z {0} is three wide at y {1}")
  @CsvSource({"23, 9", "24, 8", "25, 7", "26, 6"})
  void theMainFlightIsThreeWide(final int z, final int y) {
    for (int x = Manor.MID_X - 1; x <= Manor.MID_X + 1; x++) {
      assertTrue(t.get(x, y, z).contains("dark_oak_stairs[facing=north"), t.get(x, y, z));
    }
    assertTrue(t.get(Manor.MID_X - 2, y, z).contains("fence"));
  }

  @ParameterizedTest(
      name = "the return flight at x {0} climbs south to the gallery, y {1} at z {2}")
  @CsvSource({"32, 10, 20", "34, 11, 21", "42, 12, 22", "44, 10, 20"})
  void returnFlightsReachTheGalleries(final int x, final int y, final int z) {
    assertTrue(t.get(x, y, z).contains("dark_oak_stairs[facing=south"), t.get(x, y, z));
    assertEquals(BlockState.AIR, t.get(x, y + 1, z));
  }

  @ParameterizedTest(name = "the gallery floor at {0},{1} is railed at x {2}")
  @CsvSource({"33, 30, 35", "43, 30, 41"})
  void galleriesAreThreeWideAndRailed(final int x, final int z, final int rail) {
    assertEquals(Manor.PLANKS, t.get(x, Manor.F1, z));
    assertTrue(t.get(rail, Manor.F1 + 1, z).contains("fence"));
    assertEquals(3, Math.abs(rail - (rail < Manor.MID_X ? StairHall.WEST : StairHall.EAST)));
  }

  @ParameterizedTest(name = "under the landing at {0},{1},{2} is solid")
  @CsvSource({"32, 6, 18", "44, 8, 22", "38, 7, 20"})
  void nothingIsLeftLowUnderTheLanding(final int x, final int y, final int z) {
    assertTrue(!BlockState.AIR.equals(t.get(x, y, z)));
  }

  @ParameterizedTest(name = "a lit chandelier hangs at z {0}")
  @CsvSource({"29", "35"})
  void litChandeliersHangOverTheHall(final int z) {
    assertTrue(t.get(Manor.MID_X, Manor.TOP - 1, z).contains("lit=true"));
  }
}
