package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class VampirePoseTest {

  @ParameterizedTest(name = "downed {0}, levitating {1}, landing {2}: {6}")
  @CsvSource({
    "true, true, false, false, 0.9, 0.3, DOWNED, downed",
    "false, true, false, false, 0.0, 0.0, LEVITATE, levitate",
    "false, true, false, false, 0.0, 0.1, LEVITATE_MOVE, levitate_move",
    "false, true, false, false, 0.0, 0.3, LEVITATE_SPRINT, levitate_sprint",
    "false, true, false, true, 0.0, 0.1, LEVITATE_SPRINT, levitate_sprint",
    "false, false, true, false, 0.9, 0.3, LAND, land",
    "false, false, false, false, 0.9, 0.3, RUN, run",
    "false, false, false, false, 0.3, 0.1, WALK, walk",
    "false, false, false, false, 0.05, 0.0, IDLE, idle"
  })
  @DisplayName("downed wins over levitating, which wins over landing and moving")
  void pose(
      final boolean downed,
      final boolean levitating,
      final boolean landing,
      final boolean sprinting,
      final float swing,
      final double speed,
      final VampirePose pose,
      final String animation) {
    assertEquals(
        pose,
        VampirePose.of(new VampirePose.Cue(downed, levitating, landing, sprinting, swing, speed)));
    assertEquals(animation, pose.animation());
  }

  @ParameterizedTest(name = "sprinting {0}, speed {1}: spread {2}")
  @CsvSource({
    "false, 0.0, 0.0",
    "false, 0.06, 0.25",
    "false, 0.12, 0.5",
    "false, 0.24, 1.0",
    "false, 0.5, 1.0",
    "false, -0.1, 0.0",
    "true, 0.0, 1.0"
  })
  @DisplayName("arms spread with speed until a sprint, where they are fully out")
  void spread(final boolean sprinting, final double speed, final double spread) {
    assertEquals(spread, VampirePose.spread(sprinting, speed), 1.0e-9);
  }

  @ParameterizedTest(name = "swinging {0}, using {1}, right arm {2}: {3}")
  @CsvSource({
    "false, false, true, ",
    "true, false, true, ATTACK",
    "true, false, false, ATTACK",
    "false, true, true, USE_ITEM_RIGHT",
    "false, true, false, USE_ITEM_LEFT",
    "true, true, true, USE_ITEM_RIGHT"
  })
  @DisplayName("using an item wins over a swing, and plays on the arm holding it")
  void action(
      final boolean swinging, final boolean using, final boolean rightArm, final VampirePose pose) {
    assertEquals(Optional.ofNullable(pose), VampirePose.action(swinging, using, rightArm));
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
