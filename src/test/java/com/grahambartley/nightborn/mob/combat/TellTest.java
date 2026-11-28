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

  @ParameterizedTest(name = "engaged {0}, helps an ally {1}, at tick {2}: {3}")
  @CsvSource({
    "true, false, 100, WAIT",
    "true, false, 111, WAIT",
    "true, false, 112, CAST",
    "true, false, 140, CAST",
    "false, false, 100, DROP",
    "false, false, 112, DROP",
    "false, true, 100, WAIT",
    "false, true, 112, CAST"
  })
  @DisplayName(
      "a tell lands as a cast once its time is up, and is dropped once the mob is no longer"
          + " fighting or escaping unless it is helping an ally")
  void step(final boolean engaged, final boolean helps, final long now, final Tell.Step step) {
    final Tell tell = helps ? new Tell(RUSH, 112, Aim.ALLY, ALLY) : new Tell(RUSH, 112, Aim.TARGET);
    assertEquals(step, tell.step(engaged, now));
  }
}
