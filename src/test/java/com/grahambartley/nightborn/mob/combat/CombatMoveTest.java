package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.config.CombatSettings;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.mob.ShippedClips;
import com.grahambartley.nightborn.mob.stage.StageCombat;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class CombatMoveTest {

  private static final CombatSettings SETTINGS = NightbornConfig.DEFAULTS.combat();
  private static final StageCombat ELDER = new StageCombat(3, 20, 45, 35, 30, 50, 35);

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
            distance, onGround, new CombatMove.Threat(swinging, aiming), roll, ELDER, SETTINGS));
  }

  @ParameterizedTest(name = "threatened {0}, was {1}, roll {2}: {3}")
  @CsvSource({
    "true, false, 0, true",
    "true, false, 49, true",
    "true, false, 50, false",
    "true, true, 0, false",
    "false, true, 0, false",
    "false, false, 0, false"
  })
  @DisplayName("a vampire dodges with a power once for each new swing or aimed shot")
  void dodgesWithPower(
      final boolean threatened, final boolean wasThreatened, final int roll, final boolean dodges) {
    assertEquals(dodges, CombatMove.dodgesWithPower(threatened, wasThreatened, roll, ELDER));
    assertEquals(
        false, CombatMove.dodgesWithPower(threatened, wasThreatened, roll, StageCombat.PLAIN));
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
        CombatMove.decide(
            distance, true, new CombatMove.Threat(true, true), 0, StageCombat.PLAIN, SETTINGS));
  }

  @ParameterizedTest(name = "leap range {0} to {1} at {2} blocks: {3}")
  @CsvSource({"3, 7, 5, LEAP", "3, 7, 9, NONE", "8, 12, 9, LEAP", "8, 12, 5, NONE"})
  @DisplayName("the server settings decide how far a leap reaches")
  void leapRangeIsASetting(final int min, final int max, final double distance, final String move) {
    final CombatSettings settings =
        NightbornConfig.DEFAULTS
            .with(CombatSettings.LEAP_MIN_RANGE, min)
            .with(CombatSettings.LEAP_MAX_RANGE, max)
            .combat();
    assertEquals(
        move.equals("NONE") ? Optional.empty() : Optional.of(CombatMove.LEAP),
        CombatMove.decide(
            distance, true, new CombatMove.Threat(false, false), 0, LEAP_ONLY, settings));
  }

  private static final StageCombat LEAP_ONLY = new StageCombat(0, 20, 100, 0, 0, 0, 0);
}
