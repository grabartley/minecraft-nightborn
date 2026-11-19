package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HunterPoseTest {

  @ParameterizedTest(name = "swing {0}, trade {1}, sprint {2}, limb swing {3}, armed {4}: {5}")
  @CsvSource({
    "true, true, true, 0.5, true, STAKE_STRIKE",
    "true, false, false, 0.0, false, STAKE_STRIKE",
    "false, true, false, 0.5, false, TRADE",
    "false, false, false, 0.0, true, IDLE",
    "false, false, true, 0.0, true, IDLE",
    "false, false, true, 0.05, true, IDLE",
    "false, false, false, 0.3, true, WALK",
    "false, false, false, 0.55, true, WALK",
    "false, false, true, 0.3, true, RUN",
    "false, false, false, 0.8, true, WALK",
    "false, false, true, 0.8, true, RUN",
    "false, false, false, 0.0, false, IDLE_CALM",
    "false, false, true, 0.05, false, IDLE_CALM",
    "false, false, false, 0.3, false, WALK_CALM",
    "false, false, false, 0.8, false, WALK_CALM",
    "false, false, true, 0.3, false, RUN"
  })
  void picksTheClipForWhatTheHunterIsDoing(
      final boolean swinging,
      final boolean trading,
      final boolean sprinting,
      final float limbSwing,
      final boolean armed,
      final HunterPose pose) {
    assertEquals(pose, HunterPose.of(swinging, trading, sprinting, limbSwing, armed));
  }

  @ParameterizedTest(name = "{0} plays {1}")
  @CsvSource({
    "IDLE, idle",
    "WALK, walk",
    "RUN, run",
    "STAKE_STRIKE, stake_strike",
    "TRADE, trade",
    "IDLE_CALM, idle_calm",
    "WALK_CALM, walk_calm"
  })
  void playsTheClipOfTheSameName(final HunterPose pose, final String clip) {
    assertEquals(clip, pose.animation());
  }

  @ParameterizedTest(name = "{0} has {1}")
  @CsvSource({
    "hunter, IDLE",
    "hunter, WALK",
    "hunter, RUN",
    "hunter, STAKE_STRIKE",
    "hunter, TRADE",
    "hunter, IDLE_CALM",
    "hunter, WALK_CALM",
    "grand_inquisitor, IDLE",
    "grand_inquisitor, WALK",
    "grand_inquisitor, RUN",
    "grand_inquisitor, STAKE_STRIKE",
    "grand_inquisitor, TRADE",
    "grand_inquisitor, IDLE_CALM",
    "grand_inquisitor, WALK_CALM"
  })
  void everyHunterRigHoldsEveryPose(final String rig, final HunterPose pose) throws IOException {
    final Path file =
        Path.of("src/client/resources/assets/nightborn/animations/entity", rig + ".animation.json");
    assertTrue(
        JsonParser.parseString(Files.readString(file))
            .getAsJsonObject()
            .getAsJsonObject("animations")
            .has(pose.animation()));
  }
}
