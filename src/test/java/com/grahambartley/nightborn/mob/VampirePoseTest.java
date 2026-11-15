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

  @ParameterizedTest(name = "in coffin {0}, downed {1}, levitating {2}, landing {3}: {7}")
  @CsvSource({
    "true, true, true, true, true, 0.9, 0.3, COFFIN_SLEEP, coffin_sleep",
    "true, false, false, false, false, 0.0, 0.0, COFFIN_SLEEP, coffin_sleep",
    "false, true, true, false, false, 0.9, 0.3, DOWNED, downed",
    "false, false, true, false, false, 0.0, 0.0, LEVITATE, levitate",
    "false, false, true, false, false, 0.0, 0.1, LEVITATE_MOVE, levitate_move",
    "false, false, true, false, false, 0.0, 0.3, LEVITATE_SPRINT, levitate_sprint",
    "false, false, true, false, true, 0.0, 0.1, LEVITATE_SPRINT, levitate_sprint",
    "false, false, false, true, false, 0.9, 0.3, LAND, land",
    "false, false, false, false, false, 0.9, 0.3, RUN, run",
    "false, false, false, false, false, 0.3, 0.1, WALK, walk",
    "false, false, false, false, false, 0.05, 0.0, IDLE, idle"
  })
  @DisplayName(
      "sleeping in a coffin wins over downed, which wins over levitating, landing and moving")
  void pose(
      final boolean inCoffin,
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
        VampirePose.of(
            new VampirePose.Cue(inCoffin, downed, levitating, landing, sprinting, swing, speed)));
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

  @ParameterizedTest(name = "speed {0}: {1}")
  @CsvSource({"0.0, WEAKENED", "0.02, WEAKENED", "0.021, WEAKENED_WALK", "0.3, WEAKENED_WALK"})
  @DisplayName("a weakened vampire stoops in place and staggers once it moves")
  void weakened(final double speed, final VampirePose pose) {
    assertEquals(pose, VampirePose.weakened(speed));
  }

  @ParameterizedTest(name = "{0}: holds head {1}")
  @CsvSource({
    "DOWNED, true",
    "RISE, true",
    "FEED, true",
    "DRAIN, true",
    "COFFIN_SLEEP, true",
    "WEAKENED, false",
    "WEAKENED_WALK, false",
    "LEVITATE, false",
    "IDLE, false"
  })
  @DisplayName(
      "the head follows the look except where the clip aims it: lying, getting up and biting")
  void holdsHead(final VampirePose pose, final boolean holds) {
    assertEquals(holds, pose.holdsHead());
  }

  @ParameterizedTest(name = "{0}: allows actions {1}")
  @CsvSource({
    "VOMIT, false",
    "DOWNED, false",
    "RISE, false",
    "FEED, false",
    "DRAIN, false",
    "COFFIN_SLEEP, false",
    "LEVITATE, true",
    "LAND, true",
    "WEAKENED, true",
    "WEAKENED_WALK, true"
  })
  @DisplayName("a swing or item use plays over every pose except those that hold both arms")
  void allowsActions(final VampirePose pose, final boolean allows) {
    assertEquals(allows, pose.allowsActions());
  }

  @ParameterizedTest(name = "{0}: shows held items {1}")
  @CsvSource({
    "COFFIN_SLEEP, false",
    "DOWNED, true",
    "FEED, true",
    "VOMIT, true",
    "LEVITATE, true",
    "IDLE, true"
  })
  @DisplayName("only sleeping in a coffin puts away what the vampire holds")
  void showsHeldItems(final VampirePose pose, final boolean shows) {
    assertEquals(shows, pose.showsHeldItems());
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
