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

  @ParameterizedTest(name = "coffin {0}, downed {1}, levitating {2}, landing {3}: {7}")
  @CsvSource({
    "COFFIN_SLEEP, true, true, true, true, 0.9, 0.3, COFFIN_SLEEP, coffin_sleep",
    "COFFIN_RISE, true, true, false, false, 0.9, 0.3, COFFIN_RISE, coffin_rise",
    "COFFIN_STEP_OUT, true, true, false, false, 0.9, 0.3, COFFIN_STEP_OUT, coffin_step_out",
    ", true, true, false, false, 0.9, 0.3, DOWNED, downed",
    ", false, true, false, false, 0.0, 0.0, LEVITATE, levitate",
    ", false, true, false, false, 0.0, 0.1, LEVITATE_MOVE, levitate_move",
    ", false, true, false, false, 0.0, 0.3, LEVITATE_SPRINT, levitate_sprint",
    ", false, true, false, true, 0.0, 0.1, LEVITATE_SPRINT, levitate_sprint",
    ", false, false, true, false, 0.9, 0.3, LAND, land",
    ", false, false, false, false, 0.9, 0.3, RUN, run",
    ", false, false, false, false, 0.3, 0.1, WALK, walk",
    ", false, false, false, false, 0.05, 0.0, IDLE, idle"
  })
  @DisplayName(
      "resting or rising in a coffin wins over downed, which wins over levitating, landing and moving")
  void pose(
      final VampirePose coffin,
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
            new VampirePose.Cue(
                Optional.ofNullable(coffin),
                downed,
                levitating,
                landing,
                sprinting,
                swing,
                speed)));
    assertEquals(animation, pose.animation());
  }

  @ParameterizedTest(name = "rising {0}, standing coffin {1}: {2}")
  @CsvSource({
    "false, false, COFFIN_SLEEP",
    "false, true, COFFIN_SLEEP",
    "true, false, COFFIN_RISE",
    "true, true, COFFIN_STEP_OUT"
  })
  @DisplayName(
      "a vampire rests until it wakes, then rises from a flat coffin or steps out of a standing one")
  void coffin(final boolean rising, final boolean standingCoffin, final VampirePose pose) {
    assertEquals(pose, VampirePose.coffin(rising, standingCoffin));
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
    "COFFIN_RISE, true",
    "COFFIN_STEP_OUT, true",
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
    "COFFIN_RISE, false",
    "COFFIN_STEP_OUT, false",
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
    "COFFIN_RISE, false",
    "COFFIN_STEP_OUT, false",
    "DOWNED, true",
    "FEED, true",
    "VOMIT, true",
    "LEVITATE, true",
    "IDLE, true"
  })
  @DisplayName("only resting or rising in a coffin puts away what the vampire holds")
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
