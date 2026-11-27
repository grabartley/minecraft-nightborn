package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.mob.look.VampireRig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import software.bernie.geckolib.animation.Animation;

class VampirePoseTest {

  @ParameterizedTest(
      name =
          "coffin {0}, downed {1}, rising {2}, dying {3}, levitating {4} as {6}, landing {5},"
              + " seated {8}, reading {9}: {10}")
  @CsvSource({
    "COFFIN_SLEEP, true, false, false, true, true, LEVITATE_SPRINT, 0.9, false, false, COFFIN_SLEEP, coffin_sleep",
    "COFFIN_RISE, true, false, false, true, false, LEVITATE_SPRINT, 0.9, false, false, COFFIN_RISE, coffin_rise",
    "COFFIN_STEP_OUT, true, false, false, true, false, LEVITATE_SPRINT, 0.9, false, false, COFFIN_STEP_OUT, coffin_step_out",
    ", true, false, false, true, false, LEVITATE_SPRINT, 0.9, false, false, DOWNED, downed",
    ", false, false, false, true, false, LEVITATE, 0.0, false, false, LEVITATE, levitate",
    ", false, false, false, true, false, LEVITATE_MOVE, 0.0, false, false, LEVITATE_MOVE, levitate_move",
    ", false, false, false, true, false, LEVITATE_SPRINT, 0.0, false, false, LEVITATE_SPRINT, levitate_sprint",
    ", false, false, false, false, true, LEVITATE_SPRINT, 0.9, false, false, LAND, land",
    ", false, false, false, false, false, LEVITATE_SPRINT, 0.9, false, false, RUN, run",
    ", false, false, false, false, false, LEVITATE_MOVE, 0.3, false, false, WALK, walk",
    ", false, false, false, false, false, LEVITATE, 0.05, false, false, IDLE, idle",
    ", false, false, false, false, false, LEVITATE, 0.05, true, false, SIT, sit",
    ", false, false, false, false, false, LEVITATE, 0.05, true, true, SIT, sit",
    ", false, false, false, true, false, LEVITATE, 0.0, true, false, LEVITATE, levitate",
    ", true, false, false, false, false, LEVITATE, 0.0, true, false, DOWNED, downed",
    ", false, false, false, false, false, LEVITATE, 0.05, false, true, READ, read",
    ", false, false, false, false, false, LEVITATE_MOVE, 0.3, false, true, WALK, walk",
    ", false, false, false, false, false, LEVITATE, 0.0, false, false, IDLE, idle",
    ", false, true, false, false, false, LEVITATE_SPRINT, 0.9, false, false, RISE, rise",
    ", true, true, false, false, false, LEVITATE, 0.0, false, false, DOWNED, downed",
    "COFFIN_SLEEP, false, true, false, false, false, LEVITATE, 0.0, false, false, COFFIN_SLEEP, coffin_sleep",
    ", false, true, true, false, false, LEVITATE, 0.0, false, false, IDLE, idle",
    ", false, true, true, false, false, LEVITATE_SPRINT, 0.9, false, false, RUN, run"
  })
  @DisplayName(
      "resting or rising in a coffin wins over downed, then getting up, levitating, landing, sitting"
          + " and moving, a dying vampire never gets up,"
          + " and a vampire reads only while it stands still")
  void pose(
      final VampirePose coffin,
      final boolean downed,
      final boolean rising,
      final boolean dying,
      final boolean levitating,
      final boolean landing,
      final VampirePose levitation,
      final float swing,
      final boolean seated,
      final boolean reading,
      final VampirePose pose,
      final String animation) {
    assertEquals(
        pose,
        VampirePose.of(
            new VampirePose.Cue(
                Optional.ofNullable(coffin),
                false,
                downed,
                rising,
                dying,
                levitating,
                landing,
                levitation,
                swing,
                seated,
                reading,
                false)));
    assertEquals(animation, pose.animation());
  }

  @ParameterizedTest(
      name =
          "coffin {0}, staked {1}, dying {2}, downed {3}, levitating {4}, swing {5}, seated {6},"
              + " burning {7}: {8}")
  @CsvSource({
    ", true, true, false, false, 0.0, false, false, STAKED_DEATH",
    ", true, true, true, false, 0.0, false, false, STAKED_DEATH",
    ", true, true, false, true, 0.9, false, true, STAKED_DEATH",
    "COFFIN_SLEEP, true, true, false, false, 0.0, false, false, COFFIN_SLEEP",
    ", true, false, false, false, 0.0, false, false, IDLE",
    ", false, true, false, false, 0.0, false, false, IDLE",
    ", false, false, false, false, 0.0, false, true, SUN_BURN",
    ", false, false, false, false, 0.3, false, true, SUN_BURN",
    ", false, false, false, false, 0.9, false, true, RUN",
    ", false, false, true, false, 0.0, false, true, DOWNED",
    ", false, false, false, true, 0.0, false, true, LEVITATE",
    ", false, false, false, false, 0.0, true, true, SIT"
  })
  @DisplayName(
      "a vampire killed by a stake falls in its own clip over everything but a coffin, and one"
          + " burning in the sun staggers in place of standing or walking but not running")
  void stakedAndBurning(
      final VampirePose coffin,
      final boolean staked,
      final boolean dying,
      final boolean downed,
      final boolean levitating,
      final float swing,
      final boolean seated,
      final boolean burning,
      final VampirePose pose) {
    assertEquals(
        pose,
        VampirePose.of(
            new VampirePose.Cue(
                Optional.ofNullable(coffin),
                staked,
                downed,
                false,
                dying,
                levitating,
                false,
                VampirePose.LEVITATE,
                swing,
                seated,
                false,
                burning)));
  }

  @ParameterizedTest(name = "{0}: gesture {1}, yields {2}")
  @CsvSource({
    "SMALL_FLINCH, true, true",
    "RECOIL_GARLIC, true, true",
    "RECOIL_CRUCIFIX, true, true",
    "WAKE_STRETCH, true, false",
    "LIGHT_CANDLE, true, false",
    "DOUSE_CANDLE, true, false",
    "START_GRAMOPHONE, true, false",
    "ADJUST_CLOTHES, false, false",
    "SUN_BURN, false, false",
    "STAKED_DEATH, false, false",
    "IDLE_LOOK, false, false",
    "ATTACK, false, false"
  })
  @DisplayName(
      "the one-off gestures are sent as cues, and only the recoils give way to whatever the"
          + " vampire is already doing")
  void gestures(final VampirePose pose, final boolean gesture, final boolean yields) {
    assertEquals(gesture, pose.gesture());
    assertEquals(yields, pose.yields());
    if (gesture) {
      assertTrue(VampirePose.gestureClips().contains(pose.animation()), pose.animation());
    }
  }

  @ParameterizedTest(name = "{0}: yields {1}")
  @CsvSource({
    "recoil_garlic, true",
    "recoil_crucifix, true",
    "idle_look, true",
    "light_candle, false",
    "blood_frenzy_tell, false",
    "mourn, false",
    "'', false"
  })
  @DisplayName("a clip sent by name gives way only when it is a recoil")
  void yieldsByName(final String clip, final boolean yields) {
    assertEquals(yields, VampirePose.yields(clip));
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
    "RECOIL_GARLIC, true",
    "RECOIL_CRUCIFIX, true",
    "FINISHER_BITE, true",
    "SMALL_FLINCH, false",
    "WEAKENED, false",
    "WEAKENED_WALK, false",
    "LEVITATE, false",
    "IDLE, false",
    "IDLE_LOOK, true",
    "IDLE_LISTEN, true",
    "SIT, false",
    "READ, true",
    "SUN_BURN, true",
    "STAKED_DEATH, true",
    "ADJUST_CLOTHES, true",
    "WAKE_STRETCH, true",
    "LIGHT_CANDLE, true",
    "DOUSE_CANDLE, true",
    "START_GRAMOPHONE, true"
  })
  @DisplayName(
      "the head follows the look except where the clip aims it: lying, getting up, biting and"
          + " glancing about")
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
    "FINISHER_BITE, false",
    "RECOIL_GARLIC, true",
    "RECOIL_CRUCIFIX, true",
    "SUN_BURN, true",
    "STAKED_DEATH, false",
    "ADJUST_CLOTHES, true",
    "LIGHT_CANDLE, true",
    "COFFIN_SLEEP, false",
    "COFFIN_RISE, false",
    "COFFIN_STEP_OUT, false",
    "LEVITATE, true",
    "LAND, true",
    "WEAKENED, true",
    "WEAKENED_WALK, true",
    "IDLE_LOOK, true",
    "IDLE_LISTEN, true",
    "SIT, true",
    "READ, false"
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
    final JsonObject animations = clips(VampireRig.sharedClips());
    pose.clip()
        .getAnimationStages()
        .forEach(stage -> assertTrue(animations.has(stage.animationName()), stage.animationName()));
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("rigsAndDownedPoses")
  @DisplayName("every rig can fall, lie breathing and get up")
  void downedShips(final VampireRig rig, final VampirePose pose) throws IOException {
    final JsonObject animations = clips(rig.clips());
    pose.clip()
        .getAnimationStages()
        .forEach(stage -> assertTrue(animations.has(stage.animationName()), stage.animationName()));
  }

  static Stream<Arguments> rigsAndDownedPoses() {
    return Arrays.stream(VampireRig.values())
        .flatMap(
            rig ->
                Stream.of(VampirePose.DOWNED, VampirePose.RISE)
                    .map(pose -> Arguments.of(rig, pose)));
  }

  private static JsonObject clips(final Identifier file) throws IOException {
    return JsonParser.parseString(
            Files.readString(
                Path.of("src/client/resources/assets", file.getNamespace())
                    .resolve(file.getPath())))
        .getAsJsonObject()
        .getAsJsonObject("animations");
  }

  @ParameterizedTest(name = "{0} plays {1}")
  @CsvSource({
    "DOWNED, downed downed_rest",
    "RISE, rise",
    "IDLE, idle",
    "ATTACK, attack",
    "STAKED_DEATH, staked_death",
    "SUN_BURN, sun_burn"
  })
  @DisplayName("the downed pose falls once and then lies breathing, the others play one clip")
  void stages(final VampirePose pose, final String stages) {
    assertEquals(
        List.of(stages.split(" ")),
        pose.clip().getAnimationStages().stream().map(stage -> stage.animationName()).toList());
  }

  @ParameterizedTest(name = "{0} stage {1} loops as {2}")
  @CsvSource({
    "DOWNED, 0, play_once",
    "DOWNED, 1, loop",
    "RISE, 0, default",
    "ATTACK, 0, hold",
    "SUN_BURN, 0, loop",
    "STAKED_DEATH, 0, hold",
    "RECOIL_GARLIC, 0, default",
    "ADJUST_CLOTHES, 0, default"
  })
  @DisplayName("the fall plays once so the breathing loop can follow it")
  void loops(final VampirePose pose, final int stage, final String loop) {
    assertEquals(
        Map.of(
                "play_once", Animation.LoopType.PLAY_ONCE,
                "loop", Animation.LoopType.LOOP,
                "default", Animation.LoopType.DEFAULT,
                "hold", Animation.LoopType.HOLD_ON_LAST_FRAME)
            .get(loop),
        pose.clip().getAnimationStages().get(stage).loopType());
  }

  @ParameterizedTest(name = "{0} is standing still: {1}")
  @CsvSource({
    "IDLE, true",
    "IDLE_LOOK, true",
    "IDLE_LISTEN, true",
    "ADJUST_CLOTHES, true",
    "SUN_BURN, false",
    "WALK, false",
    "RUN, false",
    "DOWNED, false",
    "SIT, false"
  })
  @DisplayName("only the idle poses count as standing still")
  void still(final VampirePose pose, final boolean still) {
    assertEquals(still, pose.still());
  }
}
