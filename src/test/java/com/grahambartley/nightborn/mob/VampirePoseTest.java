package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class VampirePoseTest {

  @ParameterizedTest(name = "downed {0}, attacking {1}, levitating {2}, landing {3}: {7}")
  @CsvSource({
    "true, true, true, false, false, 0.9, 0.3, DOWNED, downed",
    "false, true, true, false, false, 0.9, 0.3, ATTACK, attack",
    "false, false, true, false, false, 0.0, 0.0, LEVITATE, levitate",
    "false, false, true, false, false, 0.0, 0.1, LEVITATE_MOVE, levitate_move",
    "false, false, true, false, false, 0.0, 0.3, LEVITATE_SPRINT, levitate_sprint",
    "false, false, true, false, true, 0.0, 0.1, LEVITATE_SPRINT, levitate_sprint",
    "false, false, false, true, false, 0.9, 0.3, LAND, land",
    "false, false, false, false, false, 0.9, 0.3, RUN, run",
    "false, false, false, false, false, 0.3, 0.1, WALK, walk",
    "false, false, false, false, false, 0.05, 0.0, IDLE, idle"
  })
  @DisplayName("downed wins over attacking, which wins over levitating, landing and moving")
  void pose(
      final boolean downed,
      final boolean attacking,
      final boolean levitating,
      final boolean landing,
      final boolean sprinting,
      final float swing,
      final double speed,
      final VampirePose pose,
      final String animation) {
    assertEquals(
        pose,
        VampirePose.of(
            new VampirePose.Cue(downed, attacking, levitating, landing, sprinting, swing, speed)));
    assertEquals(animation, pose.animation());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(VampirePose.class)
  @DisplayName("every pose the vampire plays is in its animation file")
  void animationShips(final VampirePose pose) throws IOException {
    final JsonObject animations =
        JsonParser.parseString(
                Files.readString(
                    Path.of(
                        "src/client/resources/assets/nightborn/animations/entity/vampire.animation.json")))
            .getAsJsonObject()
            .getAsJsonObject("animations");
    assertTrue(animations.has(pose.animation()), pose.animation());
  }
}
