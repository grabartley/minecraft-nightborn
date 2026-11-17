package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DonjonRoomsTest {

  private static Template box() {
    return new Template(
        "vampire/test", MountainCastle.SIZE, MountainCastle.HEIGHT, MountainCastle.SIZE);
  }

  private static Template built() {
    final Template t = box();
    Crag.build(t);
    Donjon.build(t);
    DonjonRooms.build(t);
    return t;
  }

  private static long count(final Template t, final String block, final int floor) {
    return t.cells().entrySet().stream()
        .filter(cell -> cell.getKey().y() > floor && cell.getKey().y() < floor + Donjon.STOREY)
        .filter(cell -> cell.getValue().contains(block))
        .count();
  }

  @ParameterizedTest(name = "storey {0} sleeps {1}")
  @CsvSource({"0, 0", "1, 7", "2, 7", "3, 6", "4, 0", "5, 0", "6, 1"})
  void threeDormitoriesSleepTwentyAndTheEyrieOne(final int storey, final int coffins) {
    assertEquals(coffins * 2L, count(built(), "_coffin[", Donjon.floor(storey)));
  }

  @ParameterizedTest(name = "storey {0}, coffin {1} is {2}")
  @CsvSource({
    "1, 0, nightborn:dark_oak_coffin",
    "1, 1, nightborn:gothic_stone_brick_coffin",
    "2, 0, nightborn:gothic_stone_brick_coffin",
    "3, 3, nightborn:gothic_stone_brick_coffin"
  })
  void coffinsAlternateGothicStoneAndDarkOak(
      final int storey, final int index, final String coffin) {
    assertEquals(coffin, DonjonRooms.coffin(storey, index));
  }

  @ParameterizedTest(name = "storey {0} stands {1} coffins upright")
  @CsvSource({"1, 3", "2, 3", "3, 2"})
  void theTopDormitoryGivesUpACoffinForBooks(final int storey, final int upright) {
    assertEquals(upright, DonjonRooms.upright(storey));
  }

  @ParameterizedTest(name = "storey {0} holds {1}")
  @CsvSource({
    "0, ornate_table",
    "0, trophy_mount",
    "0, weapon_rack",
    "0, red_carpet",
    "1, portrait",
    "2, velvet_drape",
    "3, lectern",
    "4, bookshelf",
    "4, lectern",
    "5, weapon_rack",
    "5, chalice",
    "6, polished_blackstone_bricks",
    "6, candelabra",
    "6, bookshelf",
    "0, chandelier",
    "1, chandelier",
    "2, chandelier",
    "3, chandelier",
    "4, chandelier",
    "5, chandelier",
    "6, chandelier",
    "0, chest",
    "3, chest",
    "4, chest",
    "5, chest",
    "6, chest"
  })
  void everyFloorIsFurnishedAndLit(final int storey, final String thing) {
    assertTrue(count(built(), thing, Donjon.floor(storey)) > 0, thing);
  }

  @ParameterizedTest(name = "storey {0}")
  @CsvSource({"1", "2", "3", "4", "5", "6"})
  void nothingStandsInAStairwell(final int storey) {
    final Template t = built();
    final int arriving = Donjon.flightX(storey - 1);
    final int y = Donjon.floor(storey) + 1;
    for (int z = Donjon.FLIGHT_FOOT - Donjon.STOREY; z <= Donjon.FLIGHT_FOOT - 2; z++) {
      assertEquals(BlockState.AIR, t.get(arriving, y, z), "landing at z " + z);
    }
    if (storey < Donjon.STOREYS - 1) {
      final int leaving = Donjon.flightX(storey);
      assertTrue(t.get(leaving, y, Donjon.FLIGHT_FOOT).contains("stairs"));
      assertEquals(BlockState.AIR, t.get(leaving, y, Donjon.FLIGHT_FOOT + 1));
    }
  }

  @ParameterizedTest(name = "the eyrie has {3} at {0},{1},{2}")
  @CsvSource({
    "31, 70, 27, gothic_stone_brick_coffin[facing=north,occupied=false,open=false,part=foot",
    "31, 70, 26, gothic_stone_brick_coffin[facing=north,occupied=false,open=false,part=head",
    "31, 69, 26, polished_blackstone_bricks",
    "31, 69, 29, polished_blackstone_brick_stairs",
    "31, 69, 31, air",
    "31, 70, 31, air",
    "31, 73, 31, chandelier"
  })
  void theEldestSleepsOnADaisWithItsRoomsMiddleClear(
      final int x, final int y, final int z, final String block) {
    assertTrue(built().get(x, y, z).contains(block), built().get(x, y, z));
  }

  @ParameterizedTest(name = "storey {0}: the portrait at {1} hangs on solid wall facing {2}")
  @CsvSource({
    "1, 38, west",
    "1, 24, east",
    "2, 38, west",
    "2, 24, east",
    "3, 38, west",
    "3, 24, east",
    "6, 24, east",
    "6, 38, west"
  })
  void sidePortraitsHangOnSolidWall(final int storey, final int x, final String facing) {
    final Template t = built();
    final int y = Donjon.floor(storey) + 2;
    final int z = Donjon.NORTH + 6;
    assertTrue(t.get(x, y, z).contains("portrait"), t.get(x, y, z));
    final int back = facing.equals("west") ? x + 1 : x - 1;
    assertTrue(Turret.solid(t.get(back, y, z)), t.get(back, y, z));
  }
}
