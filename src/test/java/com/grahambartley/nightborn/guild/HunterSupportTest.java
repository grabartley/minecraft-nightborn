package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.mob.brain.ActionReadiness;
import com.grahambartley.nightborn.skill.PowerUse;
import java.util.List;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HunterSupportTest {

  private static final double WARD_RANGE = 6.0;

  @ParameterizedTest(name = "foe {0}, busy {1}, can throw {2}: ready {3}")
  @CsvSource({
    "true, false, true, true",
    "true, false, false, false",
    "true, true, true, false",
    "false, false, true, false"
  })
  @DisplayName("holy water is on offer to a hunter facing a foe with a flask ready to throw")
  void holyWater(
      final boolean foe, final boolean busy, final boolean canThrow, final boolean ready) {
    final List<ActionReadiness> actions =
        HunterSupport.actions(
            true, false, new HunterSupport.Moment(foe, 5.0, busy, canThrow, false), WARD_RANGE);
    assertEquals(
        List.of(
            new ActionReadiness(
                HunterSupport.HOLY_WATER,
                verdict(ready),
                HunterActionTimer.THROW_COOLDOWN_TICKS,
                false)),
        actions);
  }

  @ParameterizedTest(name = "foe {0}, {1} blocks, busy {2}, raised {3}: ready {4}, held {5}")
  @CsvSource({
    "true, 3.9, false, false, true, false",
    "true, 6.0, false, false, true, false",
    "true, 6.1, false, false, false, false",
    "true, 4.0, true, false, false, false",
    "false, 4.0, false, false, false, false",
    "true, 4.0, false, true, true, true",
    "true, 8.0, false, true, false, false"
  })
  @DisplayName("the crucifix is on offer at a foe in its reach, and counts as running once raised")
  void crucifix(
      final boolean foe,
      final double distance,
      final boolean busy,
      final boolean raised,
      final boolean ready,
      final boolean running) {
    assertEquals(
        List.of(new ActionReadiness(HunterSupport.CRUCIFIX, verdict(ready), 0, running)),
        HunterSupport.actions(
            false, true, new HunterSupport.Moment(foe, distance, busy, true, raised), WARD_RANGE));
  }

  @ParameterizedTest(name = "holy water {0}, crucifix {1}: {2}")
  @CsvSource({
    "true, true, 'nightborn:holy_water nightborn:crucifix'",
    "true, false, 'nightborn:holy_water'",
    "false, true, 'nightborn:crucifix'",
    "false, false, ''"
  })
  @DisplayName("a hunter is offered only what its kit carries")
  void offersWhatTheKitCarries(final boolean holyWater, final boolean crucifix, final String ids) {
    assertEquals(
        ids.isEmpty() ? List.of() : List.of(ids.split(" ")).stream().map(Identifier::of).toList(),
        HunterSupport.actions(
                holyWater,
                crucifix,
                new HunterSupport.Moment(true, 5.0, false, true, false),
                WARD_RANGE)
            .stream()
            .map(ActionReadiness::id)
            .toList());
  }

  @ParameterizedTest(name = "hunting {0}, foe {1}, sees {2}, {3} blocks: holds {4}")
  @CsvSource({
    "true, true, true, 5.0, true",
    "true, true, true, 6.0, true",
    "true, true, true, 6.5, false",
    "true, true, false, 5.0, false",
    "true, false, true, 5.0, false",
    "false, true, true, 5.0, false"
  })
  @DisplayName("a raised crucifix stays up only at a foe in sight and in reach")
  void holds(
      final boolean hunting,
      final boolean foe,
      final boolean sees,
      final double distance,
      final boolean holds) {
    assertEquals(holds, HunterSupport.holds(hunting, foe, sees, distance, WARD_RANGE));
  }

  private static PowerUse.Verdict verdict(final boolean ready) {
    return ready ? PowerUse.Verdict.READY : PowerUse.Verdict.COOLING;
  }
}
