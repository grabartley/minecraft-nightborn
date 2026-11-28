package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.mob.brain.Aim;
import java.util.UUID;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TellTest {

  private static final Identifier RUSH = Identifier.of("nightborn", "core/blood_rush");
  private static final UUID ALLY = UUID.fromString("00000000-0000-0000-0000-000000000007");

  @ParameterizedTest(
      name = "begun in a fight {0}, engaged {1}, helps an ally {2}, at tick {3}: {4}")
  @CsvSource({
    "true, true, false, 100, WAIT",
    "true, true, false, 111, WAIT",
    "true, true, false, 112, CAST",
    "true, true, false, 140, CAST",
    "true, false, false, 100, DROP",
    "true, false, false, 112, DROP",
    "true, false, true, 100, WAIT",
    "true, false, true, 112, CAST",
    "false, false, false, 100, WAIT",
    "false, false, false, 112, CAST",
    "false, true, false, 112, CAST"
  })
  @DisplayName(
      "a tell lands as a cast once its time is up, and one begun in a fight is dropped once the"
          + " mob is no longer fighting or escaping unless it is helping an ally")
  void step(
      final boolean inFight,
      final boolean engaged,
      final boolean helps,
      final long now,
      final Tell.Step step) {
    final Tell tell =
        new Tell(RUSH, 112, helps ? Aim.ALLY : Aim.TARGET, helps ? ALLY : null, inFight);
    assertEquals(step, tell.step(engaged, now));
  }
}
