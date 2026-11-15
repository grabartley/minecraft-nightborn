package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.config.NightbornConfig;
import java.util.stream.Stream;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CombatMovesTest {

  private static final double CLOSE = 1.0E-9;

  static Stream<Arguments> impulses() {
    return Stream.of(
        Arguments.of(CombatMove.LEAP, new Vec3d(4, 3, 0), true, new Vec3d(0.9, 0.45, 0)),
        Arguments.of(CombatMove.DASH, new Vec3d(0, 0, -10), true, new Vec3d(0, 0.12, -1.3)),
        Arguments.of(CombatMove.DODGE, new Vec3d(0, 0, 5), true, new Vec3d(-0.85, 0.25, 0)),
        Arguments.of(CombatMove.DODGE, new Vec3d(0, 0, 5), false, new Vec3d(0.85, 0.25, 0)),
        Arguments.of(CombatMove.LEAP, new Vec3d(0, 2, 0), true, new Vec3d(0, 0.45, 0)));
  }

  @ParameterizedTest(name = "{0} toward {1}, left {2}")
  @MethodSource("impulses")
  @DisplayName("leaps and dashes go at the target on the flat, dodges go to one side")
  void impulse(
      final CombatMove move, final Vec3d toward, final boolean left, final Vec3d expected) {
    final Vec3d impulse =
        CombatMoves.impulse(move.tuning(NightbornConfig.DEFAULTS.combat()), move, toward, left);
    assertEquals(expected.x, impulse.x, CLOSE);
    assertEquals(expected.y, impulse.y, CLOSE);
    assertEquals(expected.z, impulse.z, CLOSE);
  }
}
