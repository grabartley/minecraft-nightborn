package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class CaveRoomTest {

  @ParameterizedTest(name = "{0} lanes to the {1} of a floor")
  @CsvSource({"1, east, 0, 0", "2, east, 1, 0", "2, south, 0, 1", "3, west, -2, 0"})
  void laysEachLaneBesideTheRoute(
      final int lanes, final String side, final int lastX, final int lastZ) {
    final BlockPos floor = new BlockPos(4, -30, 9);
    final List<BlockPos> floors = new CaveRoom(3, lanes).floors(floor, Direction.byName(side));
    assertEquals(lanes, floors.size());
    assertEquals(floor, floors.getFirst());
    assertEquals(floor.add(lastX, 0, lastZ), floors.getLast());
  }

  static Stream<Arguments> boxes() {
    return Stream.of(
        Arguments.of(
            CaveRoom.PERSON,
            List.of(new BlockPos(3, -30, 7)),
            Direction.EAST,
            new BlockBox(3, -30, 7, 3, -28, 7)),
        Arguments.of(
            new CaveRoom(3, 2),
            List.of(new BlockPos(3, -30, 7)),
            Direction.EAST,
            new BlockBox(3, -30, 7, 4, -27, 7)),
        Arguments.of(
            new CaveRoom(3, 2),
            List.of(new BlockPos(0, -30, 0), new BlockPos(0, -29, -1)),
            Direction.WEST,
            new BlockBox(-1, -30, -1, 0, -26, 0)));
  }

  @ParameterizedTest(name = "{0} over {1} to the {2} takes {3}")
  @MethodSource("boxes")
  void boxesEveryLaneAndTheHeadroomAboveIt(
      final CaveRoom room, final List<BlockPos> route, final Direction side, final BlockBox box) {
    assertEquals(box, room.around(route, side));
  }

  @Test
  void refusesAnEmptyRoute() {
    assertThrows(
        IllegalArgumentException.class, () -> CaveRoom.PERSON.around(List.of(), Direction.EAST));
  }

  static Stream<Arguments> rooms() {
    return Stream.of(
        Arguments.of("{\"headroom\": 3, \"lanes\": 2}", true),
        Arguments.of("{\"headroom\": 1, \"lanes\": 2}", false),
        Arguments.of("{\"headroom\": 3, \"lanes\": 0}", false));
  }

  @ParameterizedTest(name = "{0} reads as valid {1}")
  @MethodSource("rooms")
  void readsOnlySensibleRoom(final String json, final boolean valid) {
    assertEquals(
        valid,
        CaveRoom.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).result().isPresent());
  }
}
