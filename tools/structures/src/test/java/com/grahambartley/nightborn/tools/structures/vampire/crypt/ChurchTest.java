package com.grahambartley.nightborn.tools.structures.vampire.crypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.RouteRoom;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ChurchTest {

  static Stream<Arguments> churches() {
    return Stream.of(
        Arguments.of(Church.TOWER.path(), Church.build(Church.TOWER)),
        Arguments.of(Church.BELLCOTE.path(), Church.build(Church.BELLCOTE)));
  }

  @ParameterizedTest(name = "{0} hangs from the crypt by the vestry stair")
  @MethodSource("churches")
  void theTopStepOfTheCryptStairIsLevelWithTheVestryFloor(
      final String path, final Template church) {
    final Pos stair = new Pos(Church.STAIR_X, 0, Church.STAIR_Z);
    assertEquals(
        "minecraft:jigsaw[orientation=down_north]", church.get(stair.x(), stair.y(), stair.z()));
    final Map<String, Object> data = church.blockData().get(stair);
    assertEquals(Church.VESTRY, data.get("name"));
    assertTrue(((String) data.get("final_state")).startsWith(CryptStair.STEP + "[facing=east"));
    assertEquals(BlockState.AIR, church.get(Church.STAIR_X - 1, 0, Church.STAIR_Z));
    assertEquals(BlockState.AIR, church.get(Church.STAIR_X, 1, Church.STAIR_Z));
  }

  @ParameterizedTest(name = "{0} shows nothing of the crypt from the nave")
  @MethodSource("churches")
  void theNaveFloorIsWhole(final String path, final Template church) {
    for (int x = 9; x <= 21; x++) {
      for (int z = 6; z <= 12; z++) {
        assertEquals("minecraft:polished_andesite", church.get(x, 0, z), x + "," + z);
      }
    }
  }

  @ParameterizedTest(name = "{0} is a working church with a lit altar and a cleric's brewing stand")
  @MethodSource("churches")
  void itIsAWorkingChurch(final String path, final Template church) {
    assertTrue(church.get(29, 2, 8).contains("lit=true"));
    assertTrue(church.get(29, 2, 10).contains("lit=true"));
    assertEquals("minecraft:brewing_stand", church.get(23, 1, 5));
    assertTrue(
        church.cells().values().stream().anyMatch(state -> state.startsWith("minecraft:bell")));
    assertTrue(church.get(10, 1, 11).startsWith("minecraft:water_cauldron"));
  }

  @ParameterizedTest(name = "{0} keeps every way out open for its residents")
  @MethodSource("churches")
  void residentsCanWalkOut(final String path, final Template church) {
    for (int y = 1; y <= RouteRoom.HOUSEHOLD; y++) {
      for (int x = 25; x <= 27; x++) {
        assertEquals(BlockState.AIR, church.get(x, y, 6), "vestry arch at " + x + ", " + y);
      }
      for (int x = 12; x <= 14; x++) {
        assertEquals(BlockState.AIR, church.get(x, y, 13), "nave arch at " + x + ", " + y);
      }
    }
    assertEquals(BlockState.AIR, church.get(13, RouteRoom.HOUSEHOLD, 16));
    assertEquals(BlockState.AIR, church.get(9, 1, 9));
  }

  @ParameterizedTest(name = "{0} holds no crucifix that would ward its own crypt")
  @MethodSource("churches")
  void noWardStandsOverTheCrypt(final String path, final Template church) {
    assertFalse(
        church.cells().values().stream()
            .anyMatch(state -> state.contains("crucifix") || state.contains("garlic")));
  }

  @ParameterizedTest(name = "{0} faces its pews towards the altar")
  @MethodSource("churches")
  void pewsFaceTheAltar(final String path, final Template church) {
    for (int x = 11; x <= 19; x += 2) {
      assertTrue(church.get(x, 1, 7).contains("facing=west"));
    }
  }
}
