package com.grahambartley.nightborn.house.duel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.boss.Arena;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DuelRulesTest {

  @ParameterizedTest(name = "target {0}, attacker {1}, foe {2}, attacker known {3} -> {4}")
  @CsvSource({
    "false, false, false, true, true",
    "false, false, false, false, true",
    "true, true, true, true, true",
    "true, false, false, true, false",
    "true, false, false, false, true",
    "false, true, false, true, false",
    "true, true, false, true, false"
  })
  @DisplayName("nobody outside a duel can harm a fighter, and a fighter harms only their foe")
  void allowsHarm(
      final boolean targetDueling,
      final boolean attackerDueling,
      final boolean foe,
      final boolean known,
      final boolean allowed) {
    assertEquals(allowed, DuelRules.allowsHarm(targetDueling, attackerDueling, foe, known));
  }

  @ParameterizedTest(name = "({0}, {1}) and ({2}, {3}) -> centre ({4}, {5})")
  @CsvSource({"0, 0, 4, 0, 2, 0", "-3.5, 10, 2.5, 4, -1, 7", "7.9, 7.9, 7.9, 7.9, 7, 7"})
  @DisplayName("the circle is drawn between the lord and the challenger")
  void circle(
      final double lordX,
      final double lordZ,
      final double challengerX,
      final double challengerZ,
      final int centreX,
      final int centreZ) {
    final Arena arena =
        DuelRules.circle(new Vec3d(lordX, 64, lordZ), new Vec3d(challengerX, 64, challengerZ), 8);
    assertEquals(new BlockPos(centreX, 64, centreZ), arena.centre());
    assertEquals(8, arena.radius());
  }

  @ParameterizedTest(name = "({0}, {1}) -> ({2}, {3})")
  @CsvSource({"3.5, 0.5, 10.0, 0.5", "0.5, -2.5, 0.5, -9.0", "0.5, 0.5, 10.0, 0.5"})
  @DisplayName("anyone inside the circle who is not fighting is set down just outside it")
  void pushOut(final double x, final double z, final double outX, final double outZ) {
    final Arena arena = new Arena(BlockPos.ORIGIN.up(64), 8);
    final Vec3d out = DuelRules.pushOut(arena, new Vec3d(x, 64, z));
    assertEquals(outX, out.x, 1.0E-9);
    assertEquals(outZ, out.z, 1.0E-9);
    assertEquals(false, arena.holds(out.x, out.y, out.z));
  }
}
