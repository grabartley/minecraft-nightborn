package com.grahambartley.nightborn.tools.structures.vampire.castle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GatehouseTest {

  private static final Template CASTLE = Castle.build(Castle.BASTIONS);

  @ParameterizedTest(name = "the gate is open at x {0}, z {1}")
  @CsvSource({"19, 35", "20, 37", "21, 40", "20, 38"})
  void theGateCanBeWalkedThrough(final int x, final int z) {
    assertEquals(BlockState.AIR, CASTLE.get(x, Castle.FLOOR, z));
    assertEquals(BlockState.AIR, CASTLE.get(x, Castle.FLOOR + 1, z));
    assertTrue(CASTLE.get(x, Castle.GROUND, z).contains("blackstone"));
  }

  @ParameterizedTest(name = "the gatehouse wall stands at x {0}, z {1}")
  @CsvSource({"15, 36", "25, 36", "17, 35", "23, 40"})
  void theGatehouseIsWalled(final int x, final int z) {
    final String state = CASTLE.get(x, Castle.FLOOR + 1, z);
    assertTrue(state.contains("gothic_stone_bricks") || state.contains("iron_bars"), state);
  }

  @ParameterizedTest(name = "the portcullis bar at x {0}")
  @CsvSource({"19", "20", "21"})
  void thePortcullisHangsRaised(final int x) {
    assertEquals(
        "minecraft:iron_bars",
        BlockState.parse(
                CASTLE.get(x, Castle.FLOOR + Gatehouse.GATE_HEIGHT - 2, Gatehouse.SOUTH - 2))
            .block());
  }
}
