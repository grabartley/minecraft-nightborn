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

class VampireBatPoseTest {

  @ParameterizedTest(name = "roosting {0}, biting {1}, climb {2}: {3}")
  @CsvSource({
    "true, true, -0.5, HANG, hang",
    "true, false, 0.0, HANG, hang",
    "false, true, -0.5, BITE, bite",
    "false, true, 0.0, BITE, bite",
    "false, false, -0.5, SWOOP, swoop",
    "false, false, -0.13, SWOOP, swoop",
    "false, false, -0.12, FLY, fly",
    "false, false, 0.0, FLY, fly",
    "false, false, 0.4, FLY, fly"
  })
  @DisplayName("a roosting bat hangs, a biting bat bites, a diving bat swoops, and the rest fly")
  void pose(
      final boolean roosting,
      final boolean biting,
      final double climbPerTick,
      final VampireBatPose pose,
      final String animation) {
    assertEquals(pose, VampireBatPose.of(roosting, biting, climbPerTick));
    assertEquals(animation, pose.animation());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(VampireBatPose.class)
  @DisplayName("every pose the vampire bat plays is in its animation file")
  void animationShips(final VampireBatPose pose) throws IOException {
    final JsonObject animations =
        JsonParser.parseString(
                Files.readString(
                    Path.of(
                        "src/client/resources/assets/nightborn/animations/entity/vampire_bat.animation.json")))
            .getAsJsonObject()
            .getAsJsonObject("animations");
    assertTrue(animations.has(pose.animation()), pose.animation());
  }
}
