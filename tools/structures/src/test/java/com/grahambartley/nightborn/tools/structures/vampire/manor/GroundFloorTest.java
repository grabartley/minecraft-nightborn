package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroundFloorTest {

  static Template rooms;

  @BeforeAll
  static void build() {
    rooms = new Template("vampire/test", Manor.SIZE_X, Manor.SIZE_Y, Manor.SIZE_Z);
    ManorShell.build(rooms);
    Cellar.build(rooms);
    GroundFloor.build(rooms);
  }

  @ParameterizedTest(name = "{3} at {0},{1},{2}")
  @CsvSource({
    "24, 6, 34, ornate_table",
    "22, 6, 32, ornate_chair[facing=west",
    "29, 7, 31, gramophone",
    "21, 6, 40, chest",
    "25, 11, 35, chandelier",
    "28, 6, 21, lectern",
    "25, 7, 23, candelabra",
    "21, 6, 21, chest",
    "48, 6, 35, ornate_table",
    "53, 6, 35, ornate_table",
    "47, 6, 35, ornate_chair[facing=east",
    "50, 7, 35, chalice",
    "59, 6, 42, ornate_table",
    "56, 6, 24, smoker",
    "55, 6, 18, water_cauldron",
    "49, 6, 28, chest",
    "51, 10, 25, lantern"
  })
  void furnishesEachRoomAsItIsUsed(final int x, final int y, final int z, final String block) {
    assertTrue(rooms.get(x, y, z).contains(block), rooms.get(x, y, z));
  }

  @ParameterizedTest(name = "the library shelves at {0},{1} rise {2} high")
  @CsvSource({"20, 18, 5", "25, 18, 5", "30, 20, 5", "22, 28, 5"})
  void libraryShelvesLineTheWalls(final int x, final int z, final int high) {
    for (int y = GroundFloor.SEAT; y < GroundFloor.SEAT + high; y++) {
      assertTrue(rooms.get(x, y, z).contains("bookshelf"), x + "," + y + "," + z);
    }
    assertEquals(GroundFloor.SHELF_TOP, GroundFloor.SEAT + high - 1);
  }

  @ParameterizedTest(name = "the kitchen floor is tiled at {0},{1} and the stairwell left open")
  @CsvSource({"52, 20", "55, 27", "49, 18"})
  void theKitchenIsTiledAroundTheStairwell(final int x, final int z) {
    assertTrue(rooms.get(x, Manor.F0, z).matches("minecraft:(polished_andesite|tuff_bricks)"));
    assertEquals("minecraft:air", rooms.get(Cellar.STAIR_X, Manor.F0, Cellar.STAIR_FOOT_Z + 1));
  }
}
