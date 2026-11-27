package com.grahambartley.nightborn.tools.structures.vampire.crypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.RouteRoom;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CryptStairTest {

  private static final Template SHELL = CryptStair.shell("vampire/crypt/test");

  static Stream<Arguments> steps() {
    return Stream.of(SHELL, Undercroft.build(), Ossuary.build())
        .flatMap(
            crypt ->
                IntStream.rangeClosed(1, CryptStair.TOP)
                    .mapToObj(step -> Arguments.of(crypt.path(), crypt, step)));
  }

  @ParameterizedTest(name = "in {0} step {2} climbs one block east, two lanes wide, with headroom")
  @MethodSource("steps")
  void eachStepClimbsOneBlockWithRoomToWalk(
      final String path, final Template crypt, final int step) {
    final int x = CryptStair.FOOT_X + step;
    for (int z = CryptStair.INNER_Z; z <= CryptStair.STAIR_Z; z++) {
      assertTrue(crypt.get(x, step, z).startsWith(CryptStair.STEP + "[facing=east"), "lane " + z);
      final int top = Math.min(step + 1 + RouteRoom.HOUSEHOLD, CryptStair.TOP);
      for (int y = step + 1; y <= top; y++) {
        assertEquals(BlockState.AIR, crypt.get(x, y, z), "headroom at " + y + " in lane " + z);
      }
    }
  }

  @Test
  void theJigsawUnderTheTopStepHangsTheChurchAbove() {
    final Pos top = new Pos(CryptStair.SIZE_X - 1, CryptStair.TOP, CryptStair.STAIR_Z);
    assertEquals("minecraft:jigsaw[orientation=up_north]", SHELL.get(top.x(), top.y(), top.z()));
    final Map<String, Object> data = SHELL.blockData().get(top);
    assertEquals(Church.VESTRY, data.get("target"));
    assertEquals(CryptStair.POOL, data.get("pool"));
    assertEquals("aligned", data.get("joint"));
    assertEquals(CryptStair.MASONRY.base(), data.get("final_state"));
  }

  @Test
  void theCryptLinesUpUnderTheVestryStair() {
    assertEquals(Church.STAIR_X, CryptStair.SIZE_X - 1 + CryptStair.OFFSET_X);
    assertEquals(Church.STAIR_Z, CryptStair.STAIR_Z + CryptStair.OFFSET_Z);
  }

  @Test
  void theCryptIsAnchoredAtGroundLevelUnderTheChurch() {
    final Map<String, Object> anchor =
        SHELL.blockData().get(new Pos(CryptStair.ANCHOR_X, CryptStair.TOP, CryptStair.ANCHOR_Z));
    assertEquals(CryptStair.GROUND, anchor.get("name"));
  }

  @Test
  void theFootOfTheStairIsLevelFloor() {
    assertEquals(BlockState.AIR, SHELL.get(CryptStair.FOOT_X, 1, CryptStair.STAIR_Z));
    assertTrue(SHELL.get(CryptStair.FOOT_X, 0, CryptStair.STAIR_Z).contains("tuff"));
  }
}
