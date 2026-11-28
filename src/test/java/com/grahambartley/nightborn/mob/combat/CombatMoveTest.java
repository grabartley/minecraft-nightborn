package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.config.CombatSettings;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.mob.ShippedClips;
import com.grahambartley.nightborn.mob.brain.ActionReadiness;
import com.grahambartley.nightborn.mob.stage.StageCombat;
import com.grahambartley.nightborn.mob.stage.StageStuck;
import com.grahambartley.nightborn.skill.PowerUse;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class CombatMoveTest {

  private static final CombatSettings SETTINGS = NightbornConfig.DEFAULTS.combat();
  private static final StageCombat ELDER =
      new StageCombat(3, 20, 45, 35, 30, 50, 35, StageStuck.PLAIN);
  private static final int REST = 60;

  @ParameterizedTest(name = "{0}, free {1}, roll {2}: ready {3}")
  @CsvSource({
    "LEAP, true, 44, true",
    "LEAP, true, 45, false",
    "DASH, true, 34, true",
    "DASH, true, 35, false",
    "DODGE, true, 29, true",
    "DODGE, true, 30, false",
    "LEAP, false, 0, false",
    "DODGE, false, 0, false"
  })
  @DisplayName("a move is on offer to the brain only while free and by its stage's chance")
  void readiness(final CombatMove move, final boolean free, final int roll, final boolean ready) {
    final ActionReadiness readiness = move.readiness(free, () -> roll, ELDER, REST);
    assertEquals(move.id(), readiness.id());
    assertEquals(ready ? PowerUse.Verdict.READY : PowerUse.Verdict.COOLING, readiness.verdict());
    assertEquals(REST, readiness.cooldownTicks());
    assertFalse(readiness.running());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(CombatMove.class)
  @DisplayName("a stage with no move chances never offers a move")
  void plainNeverOffers(final CombatMove move) {
    assertEquals(
        PowerUse.Verdict.COOLING, move.readiness(true, () -> 0, StageCombat.PLAIN, REST).verdict());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(CombatMove.class)
  @DisplayName("each move is found by the id of its data file")
  void byId(final CombatMove move) {
    assertEquals(Identifier.of("nightborn", move.clip()), move.id());
    assertEquals(Optional.of(move), CombatMove.byId(move.id()));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"nightborn:holy_water", "minecraft:leap", "nightborn:pounce"})
  @DisplayName("an id that is not a move finds nothing")
  void byIdUnknown(final String id) {
    assertEquals(Optional.empty(), CombatMove.byId(Identifier.of(id)));
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
  @DisplayName("a vampire reacts once to each new swing or aimed shot, by its stage's chance")
  void reacts(
      final boolean threatened, final boolean wasThreatened, final int roll, final boolean dodges) {
    assertEquals(dodges, CombatMove.reacts(threatened, wasThreatened, () -> roll, ELDER));
    assertEquals(
        false, CombatMove.reacts(threatened, wasThreatened, () -> roll, StageCombat.PLAIN));
  }

  @ParameterizedTest(name = "threatened {0}, was {1}: rolls {2}")
  @CsvSource({"true, false, 1", "true, true, 0", "false, true, 0", "false, false, 0"})
  @DisplayName("the dice are only thrown for a new threat")
  void rollsOnlyForANewThreat(
      final boolean threatened, final boolean wasThreatened, final int rolls) {
    final int[] thrown = new int[1];
    CombatMove.reacts(
        threatened,
        wasThreatened,
        () -> {
          thrown[0]++;
          return 0;
        },
        ELDER);
    assertEquals(rolls, thrown[0]);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(CombatMove.class)
  @DisplayName("each move plays a clip the vampire rig ships")
  void clip(final CombatMove move) {
    assertTrue(ShippedClips.vampire().contains(move.clip()), move.clip());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"LEAP, 90, 45", "DASH, 130, 12", "DODGE, 85, 25"})
  @DisplayName("each move lands with its push and lift from the server settings")
  void tuning(final CombatMove move, final int push, final int lift) {
    assertEquals(new CombatMove.Tuning(push, lift), move.tuning(SETTINGS));
    assertEquals(push / 100.0, move.tuning(SETTINGS).pushBlocks(), 1.0E-9);
    assertEquals(lift / 100.0, move.tuning(SETTINGS).liftBlocks(), 1.0E-9);
  }
}
