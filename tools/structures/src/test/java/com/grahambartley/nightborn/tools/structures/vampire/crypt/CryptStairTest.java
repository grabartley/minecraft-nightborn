package com.grahambartley.nightborn.tools.structures.vampire.crypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CryptStairTest {

  private static final Template SHELL = CryptStair.shell("vampire/crypt/test");

  static IntStream steps() {
    return IntStream.range(1, CryptStair.TOP);
  }

  @ParameterizedTest(name = "step {0} climbs one block east with headroom")
  @MethodSource("steps")
  void eachStepClimbsOneBlockWithRoomToWalk(final int step) {
    final int x = CryptStair.FOOT_X + step;
    assertTrue(SHELL.get(x, step, CryptStair.STAIR_Z).startsWith(CryptStair.STEP + "[facing=east"));
    assertEquals(BlockState.AIR, SHELL.get(x, step + 1, CryptStair.STAIR_Z));
    assertEquals(
        BlockState.AIR, SHELL.get(x, Math.min(step + 2, CryptStair.TOP), CryptStair.STAIR_Z));
  }

  @Test
  void theTopStepIsTheJigsawThatHangsTheChurchAbove() {
    final Pos top = new Pos(CryptStair.SIZE_X - 1, CryptStair.TOP, CryptStair.STAIR_Z);
    assertEquals("minecraft:jigsaw[orientation=up_north]", SHELL.get(top.x(), top.y(), top.z()));
    final Map<String, Object> data = SHELL.blockData().get(top);
    assertEquals(Church.VESTRY, data.get("target"));
    assertEquals(CryptStair.POOL, data.get("pool"));
    assertEquals("aligned", data.get("joint"));
    assertTrue(((String) data.get("final_state")).startsWith(CryptStair.STEP));
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
