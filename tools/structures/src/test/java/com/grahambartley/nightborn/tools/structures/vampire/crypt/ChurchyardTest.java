package com.grahambartley.nightborn.tools.structures.vampire.crypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class ChurchyardTest {

  private static final Template BANK = Churchyard.bank(Undercroft.build());
  private static final Template CHURCH = Church.build(Church.TOWER);

  static Stream<Arguments> edges() {
    return Stream.of(
        Arguments.of(0, 10), Arguments.of(Churchyard.YARD_X - 1, 10),
        Arguments.of(15, 0), Arguments.of(15, Churchyard.YARD_Z - 1));
  }

  @ParameterizedTest(name = "the bank at {0},{1} is faced with a rubble retaining wall")
  @MethodSource("edges")
  void theEdgesAreRetainingWalls(final int x, final int z) {
    for (int y = 0; y < Churchyard.DEPTH; y++) {
      final String state = BANK.get(x, y, z);
      assertTrue(state.contains("cobblestone") || state.contains("andesite"), state);
    }
  }

  @ParameterizedTest(name = "inside the bank at {0},{1} the top is soil over stone")
  @CsvSource({"3, 18", "34, 3", "6, 20"})
  void theBankIsSoilOverStone(final int x, final int z) {
    assertEquals("minecraft:dirt", BANK.get(x, Churchyard.DEPTH - 1, z));
    assertEquals("minecraft:stone", BANK.get(x, 0, z));
  }

  @Test
  void theCryptSitsInsideTheBankUnderTheVestry() {
    final Pos jigsaw = new Pos(Church.STAIR_X, CryptStair.TOP, Church.STAIR_Z);
    assertEquals(
        "minecraft:jigsaw[orientation=up_north]", BANK.get(jigsaw.x(), jigsaw.y(), jigsaw.z()));
    assertEquals(Church.VESTRY, BANK.blockData().get(jigsaw).get("target"));
  }

  @Test
  void everyStepHasHeadroomUpThroughTheVestryFloor() {
    for (int step = 1; step <= CryptStair.TOP; step++) {
      final int x = CryptStair.OFFSET_X + CryptStair.FOOT_X + step;
      for (int y = step + 1; y <= step + 3; y++) {
        final String above =
            y < Churchyard.DEPTH
                ? BANK.get(x, y, Church.STAIR_Z)
                : CHURCH.get(x, y - Churchyard.DEPTH, Church.STAIR_Z);
        assertTrue(
            above.equals("minecraft:air") || above.startsWith("minecraft:jigsaw"),
            "step " + step + " has " + above + " at height " + y);
      }
    }
  }

  @Test
  void theTopStepMeetsTheVestryFloorWithoutAJump() {
    final String top =
        (String)
            CHURCH.blockData().get(new Pos(Church.STAIR_X, 0, Church.STAIR_Z)).get("final_state");
    assertTrue(top.startsWith(CryptStair.STEP + "[facing=east,half=bottom"), top);
    assertTrue(
        BANK.get(Church.STAIR_X - 1, CryptStair.TOP, Church.STAIR_Z).startsWith(CryptStair.STEP));
    assertTrue(
        CHURCH.get(Church.STAIR_X, 0, Church.STAIR_Z - 1).startsWith(CryptStair.STEP),
        "the second lane tops out beside it");
    assertEquals("minecraft:stone_bricks", CHURCH.get(Church.STAIR_X, 0, Church.STAIR_Z + 1));
    assertEquals("minecraft:stone_bricks", CHURCH.get(Church.STAIR_X, 0, Church.STAIR_Z - 2));
    assertEquals("minecraft:air", CHURCH.get(Church.STAIR_X, 1, Church.STAIR_Z));
    assertEquals("minecraft:air", CHURCH.get(Church.STAIR_X, 1, Church.STAIR_Z - 1));
  }

  @Test
  void theBankCoversTheChurchyardExactly() {
    assertEquals(CHURCH.sizeX(), BANK.sizeX());
    assertEquals(Church.YARD_Z, BANK.sizeZ());
    for (int x = 0; x < Churchyard.YARD_X; x++) {
      for (int z = 0; z < Churchyard.YARD_Z; z++) {
        assertTrue(CHURCH.get(x, 0, z) != null, "the church covers its yard at " + x + "," + z);
      }
    }
  }
}
