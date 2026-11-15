package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.mob.ShippedClips;
import com.grahambartley.nightborn.mob.stage.StageCombat;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class CombatMoveTest {

  private static final StageCombat ELDER = new StageCombat(3, 20, 45, 35, 30, 35);

  @ParameterizedTest(name = "{0} blocks, ground {1}, swing {2}, aim {3}, roll {4}: {5}")
  @CsvSource({
    "5, true, false, false, 10, LEAP",
    "5, true, false, false, 44, LEAP",
    "5, true, false, false, 45, NONE",
    "10, true, false, false, 34, DASH",
    "10, true, false, false, 35, NONE",
    "2, true, true, false, 10, DODGE",
    "2, true, true, false, 30, NONE",
    "12, true, false, true, 10, DODGE",
    "3, true, false, true, 10, LEAP",
    "5, false, false, false, 0, NONE",
    "20, true, false, false, 0, NONE",
    "1, true, false, false, 0, NONE"
  })
  @DisplayName("a vampire leaps in close, dashes from afar and dodges a swing or an aimed shot")
  void decide(
      final double distance,
      final boolean onGround,
      final boolean swinging,
      final boolean aiming,
      final int roll,
      final String move) {
    assertEquals(
        move.equals("NONE") ? Optional.empty() : Optional.of(CombatMove.valueOf(move)),
        CombatMove.decide(
            distance, onGround, new CombatMove.Threat(swinging, aiming), roll, ELDER));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(CombatMove.class)
  @DisplayName("each move plays a clip the vampire rig ships")
  void clip(final CombatMove move) {
    assertTrue(ShippedClips.vampire().contains(move.clip()), move.clip());
  }

  @ParameterizedTest(name = "{0} blocks")
  @CsvSource({"2", "5", "10", "14"})
  @DisplayName("a stage with no move chances never moves")
  void plainNeverMoves(final double distance) {
    assertEquals(
        Optional.empty(),
        CombatMove.decide(distance, true, new CombatMove.Threat(true, true), 0, StageCombat.PLAIN));
  }
}
