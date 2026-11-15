package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TellTest {

  private static final Tell RUSH = new Tell(Identifier.of("nightborn", "core/blood_rush"), 112);

  @ParameterizedTest(name = "target {0} at tick {1}: {2}")
  @CsvSource({
    "true, 100, WAIT",
    "true, 111, WAIT",
    "true, 112, CAST",
    "true, 140, CAST",
    "false, 100, DROP",
    "false, 112, DROP"
  })
  @DisplayName("a tell lands as a cast once its time is up, and is dropped if the target goes")
  void step(final boolean hasTarget, final long now, final Tell.Step step) {
    assertEquals(step, RUSH.step(hasTarget, now));
  }
}
