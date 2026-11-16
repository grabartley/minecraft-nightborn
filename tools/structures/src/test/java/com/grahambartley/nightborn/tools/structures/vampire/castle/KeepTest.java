package com.grahambartley.nightborn.tools.structures.vampire.castle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class KeepTest {

  private static final Template KEEP = keep();

  private static Template keep() {
    final Template t = new Template("vampire/test", Castle.SIZE, Castle.HEIGHT, Castle.SIZE);
    Keep.build(t);
    return t;
  }

  @ParameterizedTest(name = "flight from x {0} climbs {1} to {2}")
  @CsvSource({"13, 2, 8", "26, 9, 14"})
  void stairsClimbFromFloorToFloor(final int x, final int from, final int to) {
    for (int y = from; y <= to; y++) {
      final int z = Keep.SCREEN - 2 - (y - from);
      for (int dx = 0; dx < 2; dx++) {
        assertTrue(KEEP.get(x + dx, y, z).contains("stairs[facing=north"), KEEP.get(x + dx, y, z));
        assertEquals(BlockState.AIR, KEEP.get(x + dx, y + 1, z));
      }
    }
    assertEquals("minecraft:" + Castle.PLANKS, KEEP.get(x, to, Keep.SCREEN - 2 - (to - from) - 1));
  }

  static Stream<Arguments> windows() {
    return Arrays.stream(Keep.HALL_WINDOWS_Z)
        .boxed()
        .flatMap(z -> Stream.of(Arguments.of(Keep.WEST, z), Arguments.of(Keep.EAST, z)));
  }

  @ParameterizedTest(name = "a tall window at x {0}, z {1}")
  @MethodSource("windows")
  void theHallIsLitByTallWindows(final int x, final int z) {
    for (int y = Castle.FLOOR + 4; y < Castle.FLOOR + 12; y++) {
      assertTrue(KEEP.get(x, y, z).startsWith(Castle.GLASS), KEEP.get(x, y, z));
    }
  }

  @ParameterizedTest(name = "the hall door at x {0}")
  @CsvSource({"19", "20", "21"})
  void theHallOpensOntoTheCourtyard(final int x) {
    assertEquals(BlockState.AIR, KEEP.get(x, Castle.FLOOR, Keep.SOUTH));
    assertEquals(BlockState.AIR, KEEP.get(x, Castle.FLOOR + 1, Keep.SOUTH));
  }

  @ParameterizedTest(name = "the screen door at x {0}")
  @CsvSource({"14", "26"})
  void theScreenOpensToTheRoomsBehindTheThrone(final int x) {
    assertEquals(BlockState.AIR, KEEP.get(x, Castle.FLOOR, Keep.SCREEN));
    assertEquals(BlockState.AIR, KEEP.get(x, Castle.FLOOR + 1, Keep.SCREEN));
  }

  @ParameterizedTest(name = "a column at z {0}")
  @CsvSource({"21", "25", "29"})
  void columnsCarryTheHallCeiling(final int z) {
    assertEquals("minecraft:" + Keep.CAPITAL, KEEP.get(Castle.MID - 5, Castle.FLOOR, z));
    assertEquals("minecraft:" + Keep.CAPITAL, KEEP.get(Castle.MID + 5, Keep.CEILING - 1, z));
    assertEquals("minecraft:" + Castle.PLANKS, KEEP.get(Castle.MID, Keep.CEILING, z));
  }
}
