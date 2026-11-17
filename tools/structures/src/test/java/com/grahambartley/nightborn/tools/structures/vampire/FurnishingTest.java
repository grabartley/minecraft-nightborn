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

  @ParameterizedTest(name = "a chest asking for {0} holds {1}")
  @CsvSource({
    ", nightborn:chests/vampire_structure",
    "nightborn:chests/castle, nightborn:chests/castle"
  })
  void chestsHoldTheirStructuresLoot(final String asked, final String held) {
    final Template t = new Template("vampire/test", 7, 4, 7);
    if (asked == null) {
      Furnishing.chest(t, 1, 1, 1, "east");
    } else {
      Furnishing.chest(t, 1, 1, 1, "east", asked);
    }
    assertEquals(held, t.blockData().get(new Pos(1, 1, 1)).get("LootTable"));
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

  @ParameterizedTest(name = "a lectern facing {0} holds a book")
  @CsvSource({"north", "west"})
  void lecternsHoldABook(final String facing) {
    final Template t = new Template("vampire/test", 5, 4, 5);
    Furnishing.lectern(t, 2, 1, 2, facing);
    assertTrue(t.get(2, 1, 2).contains("facing=" + facing));
    assertTrue(t.get(2, 1, 2).contains("has_book=true"));
    assertEquals(
        Furnishing.BOOK,
        ((java.util.Map<?, ?>) t.blockData().get(new Pos(2, 1, 2)).get("Book")).get("id"));
  }

  @ParameterizedTest(name = "a portrait at {0},{1},{2} hangs facing {3}")
  @CsvSource({"1, 1, 1, east", "2, 3, 1, west", "4, 2, 3, south"})
  void portraitsHangOneOfThreeSitters(final int x, final int y, final int z, final String facing) {
    final Template t = new Template("vampire/test", 6, 5, 6);
    Furnishing.portrait(t, x, y, z, facing);
    assertTrue(t.get(x, y, z).startsWith(Furnishing.PORTRAIT + "["));
    assertTrue(t.get(x, y, z).contains("facing=" + facing));
    assertTrue(t.get(x, y, z).matches(".*portrait=[0-2].*"), t.get(x, y, z));
  }

  @Test
  void portraitsVaryAcrossAWall() {
    final Template t = new Template("vampire/test", 1, 1, 30);
    final java.util.Set<String> sitters = new java.util.HashSet<>();
    for (int z = 0; z < 30; z++) {
      Furnishing.portrait(t, 0, 0, z, "east");
      sitters.add(t.get(0, 0, z).replaceAll(".*portrait=(\\d).*", "$1"));
    }
    assertEquals(Furnishing.PORTRAITS, sitters.size());
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

  @ParameterizedTest(name = "a gramophone at {0},{1},{2} facing {3} holds a disc it starts itself")
  @CsvSource({"1, 1, 1, east", "2, 3, 1, west", "4, 2, 3, south"})
  void gramophonesComeLoaded(final int x, final int y, final int z, final String facing) {
    final Template t = new Template("vampire/test", 6, 5, 6);
    Furnishing.gramophone(t, x, y, z, facing);
    final java.util.Map<String, Object> data = t.blockData().get(new Pos(x, y, z));
    assertTrue(t.get(x, y, z).startsWith(Furnishing.GRAMOPHONE + "["));
    assertTrue(t.get(x, y, z).contains("facing=" + facing));
    assertTrue(t.get(x, y, z).contains("has_record=true"));
    assertTrue(t.get(x, y, z).contains("playing=false"));
    assertEquals(Furnishing.GRAMOPHONE, data.get("id"));
    assertEquals(true, data.get("Ambient"));
    assertTrue(Furnishing.DISCS.contains(((java.util.Map<?, ?>) data.get("RecordItem")).get("id")));
  }

  @Test
  void gramophoneDiscsVaryAcrossAHouse() {
    final Template t = new Template("vampire/test", 1, 1, 40);
    final java.util.Set<Object> discs = new java.util.HashSet<>();
    for (int z = 0; z < 40; z++) {
      Furnishing.gramophone(t, 0, 0, z, "east");
      discs.add(
          ((java.util.Map<?, ?>) t.blockData().get(new Pos(0, 0, z)).get("RecordItem")).get("id"));
    }
    assertEquals(Furnishing.DISCS.size(), discs.size());
  }

  @ParameterizedTest(name = "{0} is a disc the mod ships")
  @org.junit.jupiter.params.provider.MethodSource("discs")
  void everyDiscIsAnItemTheModShips(final String disc) {
    assertTrue(
        java.nio.file.Files.exists(
            java.nio.file.Path.of(
                "../../src/main/resources/assets/nightborn/models/item/"
                    + disc.substring(disc.indexOf(':') + 1)
                    + ".json")),
        disc);
  }

  static java.util.List<String> discs() {
    return Furnishing.DISCS;
  }
}
