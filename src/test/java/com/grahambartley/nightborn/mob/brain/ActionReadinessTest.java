package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.skill.PowerUse;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ActionReadinessTest {

  @ParameterizedTest(name = "ready {0}, cooldown {1}: {2}")
  @CsvSource({"true, 60, READY", "false, 60, COOLING", "true, 0, READY", "false, 160, COOLING"})
  @DisplayName("an action is ready or cooling, keeps its cooldown, and is never running by default")
  void of(final boolean ready, final int cooldown, final PowerUse.Verdict verdict) {
    final Identifier id = Identifier.of("nightborn", "leap");
    assertEquals(
        new ActionReadiness(id, verdict, cooldown, false), ActionReadiness.of(id, ready, cooldown));
  }
}
