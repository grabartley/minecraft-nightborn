package com.grahambartley.nightborn.tools.structures.vampire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FurnishingTest {

  @ParameterizedTest(name = "splatter {1} lies on the floor facing {0}")
  @CsvSource({"north, 0", "east, 3"})
  void splatterLiesOnTheFloor(final String facing, final int splat) {
    final String state = Furnishing.splatter(facing, splat);
    assertTrue(state.startsWith("nightborn:blood_splatter"));
    assertTrue(state.contains("face=floor"));
    assertTrue(state.contains("facing=" + facing));
    assertTrue(state.contains("splat=" + splat));
  }

  @ParameterizedTest(name = "a coffin facing {0} puts its head at {1},{2}")
  @CsvSource({"north, 3, 2", "south, 3, 4", "east, 4, 3", "west, 2, 3"})
  void aLyingCoffinPutsItsHeadTheWayItFaces(final String facing, final int x, final int z) {
    final Template t = new Template("vampire/test", 7, 4, 7);
    Furnishing.coffin(t, "nightborn:oak_coffin", 3, 1, 3, facing);
    assertTrue(t.get(3, 1, 3).contains("part=foot"));
    assertTrue(t.get(x, 1, z).contains("part=head"));
    assertTrue(
        t.get(x, 1, z).contains("facing=" + facing + ",")
            && t.get(x, 1, z).contains("standing=false"));
  }

  @Test
  void aStandingCoffinPutsItsHeadAbove() {
    final Template t = new Template("vampire/test", 7, 4, 7);
    Furnishing.standingCoffin(t, "nightborn:oak_coffin", 3, 1, 3, "east");
    assertTrue(t.get(3, 1, 3).contains("part=foot") && t.get(3, 1, 3).contains("standing=true"));
    assertTrue(t.get(3, 2, 3).contains("part=head") && t.get(3, 2, 3).contains("facing=east"));
  }

  @Test
  void chestsHoldVampireLoot() {
    final Template t = new Template("vampire/test", 7, 4, 7);
    Furnishing.chest(t, 1, 1, 1, "east");
    assertEquals(Furnishing.LOOT, t.blockData().get(new Pos(1, 1, 1)).get("LootTable"));
  }

  @ParameterizedTest(name = "a drape {0} tall marks only its top block as the top")
  @CsvSource({"1", "3"})
  void drapesKnowTheirTop(final int height) {
    final Template t = new Template("vampire/test", 7, 6, 7);
    Furnishing.drape(t, "nightborn:burgundy_velvet_drape", 2, 1, 2, height, "south");
    for (int y = 1; y <= height; y++) {
      assertTrue(t.get(2, y, 2).contains("top=" + (y == height)));
    }
  }

  @Test
  void tendedLightsStartUnlit() {
    final Template t = new Template("vampire/test", 7, 4, 7);
    Furnishing.candelabra(t, 1, 1, 1);
    Furnishing.chandelier(t, 2, 1, 1);
    Furnishing.candles(t, 3, 1, 1, 2, false);
    assertTrue(t.get(1, 1, 1).contains("lit=false"));
    assertTrue(t.get(2, 1, 1).contains("lit=false"));
    assertTrue(t.get(3, 1, 1).contains("candles=2") && t.get(3, 1, 1).contains("lit=false"));
  }
}
