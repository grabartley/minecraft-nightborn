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
    GroundFloor.build(rooms);
  }

  @ParameterizedTest(name = "{3} at {0},{1},{2}")
  @CsvSource({
    "16, 4, 22, ornate_table",
    "15, 4, 21, ornate_chair",
    "17, 4, 28, ornate_table",
    "17, 5, 28, chalice",
    "19, 4, 14, lectern",
    "14, 4, 10, ladder",
    "35, 4, 18, ornate_table",
    "35, 5, 18, candelabra",
    "34, 4, 19, ornate_chair",
    "39, 4, 10, smoker",
    "34, 4, 9, water_cauldron",
    "38, 4, 14, chest",
    "20, 4, 26, chest",
    "36, 7, 11, lantern"
  })
  void furnishesEachRoom(final int x, final int y, final int z, final String block) {
    assertTrue(rooms.get(x, y, z).contains(block), rooms.get(x, y, z));
  }

  @ParameterizedTest(name = "shelves fill {0},{1} up to y {2}")
  @CsvSource({"13, 9, 7", "21, 16, 7", "18, 16, 7"})
  void libraryShelvesRiseFourHigh(final int x, final int z, final int top) {
    for (int y = GroundFloor.SEAT; y <= top; y++) {
      assertTrue(rooms.get(x, y, z).contains("bookshelf"), rooms.get(x, y, z));
    }
  }

  @ParameterizedTest(name = "the way through {0},{1} stays clear")
  @CsvSource({
    "21, 11", "17, 16", "17, 18", "33, 19", "36, 16", "36, 14", "33, 11", "36, 9", "13, 13"
  })
  void doorwaysStayClear(final int x, final int z) {
    assertEquals("minecraft:air", rooms.get(x, GroundFloor.SEAT, z));
    assertEquals("minecraft:air", rooms.get(x, GroundFloor.SEAT + 1, z));
  }

  @ParameterizedTest(name = "the kitchen floor at {0},{1} is {2}")
  @CsvSource({"34, 10, polished_andesite", "35, 10, tuff_bricks"})
  void theKitchenIsFlagged(final int x, final int z, final String block) {
    assertEquals("minecraft:" + block, rooms.get(x, Manor.F0, z));
  }
}
