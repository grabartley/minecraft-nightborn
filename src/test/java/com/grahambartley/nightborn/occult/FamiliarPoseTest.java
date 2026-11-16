package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FamiliarPoseTest {

  @ParameterizedTest(name = "fading {0}, age {1}, swinging {2}, {3} -> {4}")
  @CsvSource({
    "true, 5, true, SCOUT, FADE",
    "false, 5, true, SCOUT, APPEAR",
    "false, 40, true, CARRY, FIGHT",
    "false, 40, false, SCOUT, SCOUT",
    "false, 40, false, CARRY, CARRY",
    "false, 40, false, FIGHT, FLOAT"
  })
  @DisplayName("a familiar shows fading first, then appearing, striking and its task")
  void of(
      final boolean fading,
      final int age,
      final boolean swinging,
      final FamiliarMode mode,
      final FamiliarPose expected) {
    assertEquals(expected, FamiliarPose.of(fading, age, swinging, mode));
  }

  @Test
  @DisplayName("every pose plays its own clip from the familiar's animation file")
  void clips() {
    assertEquals(
        List.of("appear", "fade", "fight", "scout", "carry", "float"),
        Arrays.stream(FamiliarPose.values()).map(FamiliarPose::animation).toList());
  }
}
