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

  @ParameterizedTest(name = "roosting {0}, biting {1}, hunting {2}: {3}")
  @CsvSource({
    "true, true, true, HANG, hang",
    "true, false, false, HANG, hang",
    "false, true, true, BITE, bite",
    "false, true, false, BITE, bite",
    "false, false, true, SWOOP, swoop",
    "false, false, false, FLY, fly"
  })
  @DisplayName("a roosting bat hangs, a biting bat bites, a hunting bat swoops, and the rest fly")
  void pose(
      final boolean roosting,
      final boolean biting,
      final boolean hunting,
      final VampireBatPose pose,
      final String animation) {
    assertEquals(pose, VampireBatPose.of(roosting, biting, hunting));
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
