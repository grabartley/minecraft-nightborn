package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.entity.ai.pathing.PathNodeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FootingTest {

  @ParameterizedTest(name = "feet {0} on {1}: standable {2}")
  @CsvSource({
    "WALKABLE, BLOCKED, true",
    "WALKABLE, OPEN, true",
    "OPEN, OPEN, true",
    "DANGER_FIRE, BLOCKED, true",
    "FENCE, BLOCKED, false",
    "BLOCKED, BLOCKED, false",
    "WALKABLE, FENCE, false",
    "OPEN, FENCE, false"
  })
  @DisplayName("a walk target is never inside canvas or on top of it")
  void standableOnlyClearOfCanvas(
      final PathNodeType feet, final PathNodeType floor, final boolean standable) {
    assertEquals(standable, Footing.standable(feet, floor));
  }
}
