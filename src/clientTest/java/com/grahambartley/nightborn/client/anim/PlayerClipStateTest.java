package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.mob.VampirePose;
import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.EntityPose;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PlayerClipStateTest {

  static final String POWER = "power_gaze";

  static final Map<String, Double> SECONDS =
      Map.ofEntries(
          Map.entry(VampirePose.LAND.animation(), 0.7),
          Map.entry(VampirePose.VOMIT.animation(), 2.6),
          Map.entry(VampirePose.DRAIN.animation(), 1.4),
          Map.entry(VampirePose.RISE.animation(), 1.6),
          Map.entry(VampirePose.FINISHER_BITE.animation(), 1.5),
          Map.entry(VampirePose.SMALL_FLINCH.animation(), 0.3),
          Map.entry(VampirePose.RECOIL_GARLIC.animation(), 0.9),
          Map.entry(VampirePose.RECOIL_CRUCIFIX.animation(), 1.2),
          Map.entry(VampirePose.LIGHT_CANDLE.animation(), 1.6),
          Map.entry(VampirePose.WAKE_STRETCH.animation(), 2.6),
          Map.entry(VampirePose.STAKE_THRUST_RIGHT.animation(), 0.45),
          Map.entry(VampirePose.STAKE_THRUST_LEFT.animation(), 0.45),
          Map.entry(POWER, 0.8));

  static final PlayerClipState.Lengths LENGTHS = animation -> SECONDS.getOrDefault(animation, 0.0);

  private static PlayerClipState.Situation situation(
      final boolean levitating,
      final boolean sprinting,
      final double speed,
      final boolean downed,
      final boolean feeding,
      final boolean weakened,
      final Optional<VampirePose> coffin) {
    return new PlayerClipState.Situation(
        levitating, sprinting, speed, downed, feeding, weakened, coffin, false, false);
  }

  private static PlayerClipState.Situation still() {
    return situation(false, false, 0.0, false, false, false, Optional.empty());
  }

  private static PlayerClipState.Situation levitating() {
    return situation(true, false, 0.0, false, false, false, Optional.empty());
  }

  private static String clip(
      final PlayerClipState state, final double age, final PlayerClipState.Situation situation) {
    return state.clip(age, situation, LENGTHS).map(Enum::name).orElse("VANILLA");
  }

  @ParameterizedTest(name = "{0} ticks after landing: {1}")
  @CsvSource({"0, LAND", "13, LAND", "14, VANILLA", "40, VANILLA"})
  @DisplayName("landing plays its clip once, then the player is drawn as vanilla draws it")
  void lands(final int after, final String expected) {
    final PlayerClipState state = new PlayerClipState();
    state.tick(10, true, false, false, 0.0);
    state.tick(11, false, false, false, 0.0);
    assertEquals(expected, clip(state, 11 + after, still()));
  }

  @ParameterizedTest(name = "speed {0}, sprinting {1}: {2}")
  @CsvSource({
    "0.0, false, LEVITATE",
    "0.1, false, LEVITATE_MOVE",
    "0.1, true, LEVITATE_SPRINT",
    "0.3, false, LEVITATE_SPRINT"
  })
  @DisplayName("levitating settles on the hover, move or sprint clip by speed")
  void levitates(final double speed, final boolean sprinting, final String expected) {
    final PlayerClipState state = new PlayerClipState();
    for (int tick = 0; tick < 40; tick++) {
      state.tick(tick, true, false, sprinting, speed);
    }
    assertEquals(
        expected,
        clip(state, 40, situation(true, sprinting, speed, false, false, false, Optional.empty())));
  }

  @ParameterizedTest(name = "{0} ticks after retching, levitating {1}: {2}")
  @CsvSource({"0, false, VOMIT", "51, true, VOMIT", "52, true, LEVITATE", "52, false, VANILLA"})
  @DisplayName("vomiting wins over every other clip until it ends")
  void vomits(final int after, final boolean levitating, final String expected) {
    final PlayerClipState state = new PlayerClipState();
    state.vomit(100);
    assertEquals(expected, clip(state, 100 + after, levitating ? levitating() : still()));
    assertEquals(after / OneShot.TICKS_PER_SECOND, state.vomitingSeconds(100 + after), 1e-9);
  }

  @ParameterizedTest(name = "{0} ticks after getting up: {1}")
  @CsvSource({"0, RISE", "31, RISE", "32, VANILLA", "60, VANILLA"})
  @DisplayName("getting up plays once on the tick the player stops being downed")
  void rises(final int after, final String expected) {
    final PlayerClipState state = new PlayerClipState();
    state.tick(10, false, true, false, 0.0);
    assertEquals(
        "DOWNED",
        clip(state, 10, situation(false, false, 0.0, true, false, false, Optional.empty())));
    state.tick(11, false, false, false, 0.0);
    assertEquals(expected, clip(state, 11 + after, still()));
  }

  @Test
  @DisplayName("a player seen for the first time already standing does not play getting up")
  void noRiseWithoutDowned() {
    final PlayerClipState state = new PlayerClipState();
    state.tick(10, false, false, false, 0.0);
    assertEquals("VANILLA", clip(state, 11, still()));
  }

  @ParameterizedTest(name = "{0} ticks after draining, still feeding {1}: {2}")
  @CsvSource({"0, false, DRAIN", "27, true, DRAIN", "28, true, FEED", "28, false, VANILLA"})
  @DisplayName("draining plays once over feeding, then feeding carries on if it still holds")
  void drains(final int after, final boolean feeding, final String expected) {
    final PlayerClipState state = new PlayerClipState();
    state.drain(200);
    assertEquals(
        expected,
        clip(
            state,
            200 + after,
            situation(false, false, 0.0, false, feeding, false, Optional.empty())));
  }

  @ParameterizedTest(name = "lev {0}, downed {1}, feeding {2}, weakened {3}, speed {4}: {5}")
  @CsvSource({
    "true, true, true, true, 0.0, DOWNED",
    "true, false, true, true, 0.0, FEED",
    "true, false, false, true, 0.0, LEVITATE",
    "false, false, false, true, 0.0, WEAKENED",
    "false, false, false, true, 0.1, WEAKENED_WALK",
    "false, false, true, true, 0.1, FEED",
    "false, false, false, false, 0.1, VANILLA"
  })
  @DisplayName("downed wins over feeding, which wins over levitating, which wins over weakened")
  void priority(
      final boolean levitating,
      final boolean downed,
      final boolean feeding,
      final boolean weakened,
      final double speed,
      final String expected) {
    final PlayerClipState state = new PlayerClipState();
    state.tick(0, levitating, downed, false, 0.0);
    assertEquals(
        expected,
        clip(
            state,
            1,
            situation(levitating, false, speed, downed, feeding, weakened, Optional.empty())));
  }

  @ParameterizedTest(
      name =
          "{6}, downed {0}, feeding {1}, levitating {2}, weakened {3}, retching {4}, draining {5}")
  @CsvSource({
    "false, false, false, false, false, false, COFFIN_SLEEP",
    "true, false, false, false, false, false, COFFIN_SLEEP",
    "false, true, true, false, false, false, COFFIN_RISE",
    "false, false, false, true, false, false, COFFIN_STEP_OUT",
    "false, false, false, false, true, false, COFFIN_SLEEP",
    "false, true, false, false, false, true, COFFIN_RISE",
    "true, true, true, true, true, true, COFFIN_STEP_OUT"
  })
  @DisplayName("resting or rising in a coffin wins over every other clip")
  void inCoffin(
      final boolean downed,
      final boolean feeding,
      final boolean levitating,
      final boolean weakened,
      final boolean retching,
      final boolean draining,
      final VampirePose coffin) {
    final PlayerClipState state = new PlayerClipState();
    if (retching) {
      state.vomit(0);
    }
    if (draining) {
      state.drain(0);
    }
    state.tick(0, levitating, downed, false, 0.0);
    assertEquals(
        coffin.name(),
        clip(
            state,
            1,
            situation(levitating, false, 0.0, downed, feeding, weakened, Optional.of(coffin))));
  }

  @Test
  @DisplayName("a player who never levitated or retched is drawn as vanilla draws it")
  void vanilla() {
    final PlayerClipState state = new PlayerClipState();
    state.tick(5, false, false, false, 0.0);
    assertEquals("VANILLA", clip(state, 6, still()));
    assertTrue(Double.isNaN(state.vomitingSeconds(6)));
  }

  @ParameterizedTest(name = "{0} ticks after a {1}, weakened {2}: {3}")
  @CsvSource({
    "0, RECOIL_GARLIC, false, RECOIL_GARLIC",
    "17, RECOIL_GARLIC, false, RECOIL_GARLIC",
    "18, RECOIL_GARLIC, false, VANILLA",
    "0, RECOIL_GARLIC, true, RECOIL_GARLIC",
    "18, RECOIL_GARLIC, true, WEAKENED",
    "23, RECOIL_CRUCIFIX, false, RECOIL_CRUCIFIX",
    "24, RECOIL_CRUCIFIX, false, VANILLA",
    "31, LIGHT_CANDLE, false, LIGHT_CANDLE",
    "32, LIGHT_CANDLE, false, VANILLA",
    "51, WAKE_STRETCH, true, WAKE_STRETCH",
    "52, WAKE_STRETCH, true, WEAKENED"
  })
  @DisplayName("a gesture plays once for its own length, before the stoop of a weakened vampire")
  void gestures(
      final int after, final VampirePose gesture, final boolean weakened, final String expected) {
    final PlayerClipState state = new PlayerClipState();
    state.gesture(gesture, 20);
    assertEquals(
        expected,
        clip(
            state,
            20 + after,
            situation(false, false, 0.0, false, false, weakened, Optional.empty())));
  }

  @ParameterizedTest(
      name = "{0} ticks after the finisher bite, still feeding {1}, draining {2}: {3}")
  @CsvSource({
    "0, true, false, FINISHER_BITE",
    "29, true, false, FINISHER_BITE",
    "29, true, true, FINISHER_BITE",
    "30, true, false, FEED",
    "30, false, false, VANILLA",
    "30, false, true, DRAIN",
    "5, false, false, FINISHER_BITE",
    "5, false, true, FINISHER_BITE"
  })
  @DisplayName(
      "the finisher bite plays once over feeding and draining while the bite holds, then feeding"
          + " carries on")
  void finishes(
      final int after, final boolean feeding, final boolean draining, final String expected) {
    final PlayerClipState state = new PlayerClipState();
    state.finisher(300);
    if (draining) {
      state.drain(300 + after);
    }
    assertEquals(
        expected,
        clip(
            state,
            300 + after,
            situation(false, false, 0.0, false, feeding, false, Optional.empty())));
  }

  @Test
  @DisplayName("a finisher broken by a blow does not come back when the vampire bites again")
  void brokenFinisherStaysBroken() {
    final PlayerClipState state = new PlayerClipState();
    state.finisher(300);
    clip(state, 302, situation(false, false, 0.0, false, true, false, Optional.empty()));
    assertEquals(
        "VANILLA",
        clip(state, 305, situation(false, false, 0.0, false, false, false, Optional.empty())));
    assertEquals(
        "FEED",
        clip(state, 310, situation(false, false, 0.0, false, true, false, Optional.empty())));
  }

  @Test
  @DisplayName("the finisher plays from its cue even before the feeding flag reaches the client")
  void finisherCueArrivesFirst() {
    final PlayerClipState state = new PlayerClipState();
    state.finisher(300);
    assertEquals(
        "FINISHER_BITE",
        clip(state, 300, situation(false, false, 0.0, false, false, false, Optional.empty())));
    assertEquals(
        "FINISHER_BITE",
        clip(state, 301, situation(false, false, 0.0, false, true, false, Optional.empty())));
  }

  @Test
  @DisplayName("a levitating vampire keeps floating through a recoil")
  void levitationOutranksARecoil() {
    final PlayerClipState state = new PlayerClipState();
    state.gesture(VampirePose.RECOIL_CRUCIFIX, 20);
    assertEquals("LEVITATE", clip(state, 21, levitating()));
  }

  @ParameterizedTest(name = "{0} ticks after a small flinch: {1}")
  @CsvSource({"0, SMALL_FLINCH", "5, SMALL_FLINCH", "6, VANILLA"})
  @DisplayName("a small flinch plays its own clip for its own length")
  void smallFlinch(final int after, final String expected) {
    final PlayerClipState state = new PlayerClipState();
    state.gesture(VampirePose.SMALL_FLINCH, 20);
    assertEquals(
        expected,
        clip(
            state,
            20 + after,
            situation(false, false, 0.0, false, false, false, Optional.empty())));
  }

  @ParameterizedTest(name = "downed {0}, feeding {1}, retching {2}, recoiling {3}, coffin {4}: {5}")
  @CsvSource({
    "false, false, false, false, , STAKED_DEATH",
    "true, false, false, false, , STAKED_DEATH",
    "false, true, false, false, , STAKED_DEATH",
    "false, false, true, false, , STAKED_DEATH",
    "false, false, false, true, , STAKED_DEATH",
    "true, true, true, true, , STAKED_DEATH",
    "false, false, false, false, COFFIN_SLEEP, COFFIN_SLEEP"
  })
  @DisplayName("a vampire killed by a stake falls in its own clip over everything but a coffin")
  void staked(
      final boolean downed,
      final boolean feeding,
      final boolean retching,
      final boolean recoiling,
      final VampirePose coffin,
      final String expected) {
    final PlayerClipState state = new PlayerClipState();
    if (retching) {
      state.vomit(0);
    }
    if (recoiling) {
      state.gesture(VampirePose.RECOIL_GARLIC, 0);
    }
    state.tick(0, false, downed, false, 0.0);
    assertEquals(
        expected,
        clip(
            state,
            1,
            new PlayerClipState.Situation(
                false,
                false,
                0.0,
                downed,
                feeding,
                false,
                Optional.ofNullable(coffin),
                true,
                false)));
  }

  @ParameterizedTest(
      name = "sprinting {0}, levitating {1}, feeding {2}, recoiling {3}, weakened {4}: {5}")
  @CsvSource({
    "false, false, false, false, false, SUN_BURN",
    "false, false, false, false, true, SUN_BURN",
    "true, false, false, false, false, VANILLA",
    "true, false, false, false, true, WEAKENED",
    "false, true, false, false, false, LEVITATE",
    "false, false, true, false, false, FEED",
    "false, false, false, true, false, RECOIL_CRUCIFIX"
  })
  @DisplayName(
      "a vampire burning in the sun staggers unless it sprints, and a recoil, feeding or"
          + " levitating still win over the burn")
  void sunBurns(
      final boolean sprinting,
      final boolean levitating,
      final boolean feeding,
      final boolean recoiling,
      final boolean weakened,
      final String expected) {
    final PlayerClipState state = new PlayerClipState();
    if (recoiling) {
      state.gesture(VampirePose.RECOIL_CRUCIFIX, 0);
    }
    state.tick(0, levitating, false, false, 0.0);
    assertEquals(
        expected,
        clip(
            state,
            1,
            new PlayerClipState.Situation(
                levitating,
                sprinting,
                0.0,
                false,
                feeding,
                weakened,
                Optional.empty(),
                false,
                true)));
  }

  @ParameterizedTest(name = "{0} ticks after a recoil while downed {1}, feeding {2}: {3}")
  @CsvSource({"0, true, false, DOWNED", "0, false, true, FEED", "5, true, true, DOWNED"})
  @DisplayName("a recoil never cuts into being downed or feeding")
  void recoilYields(
      final int after, final boolean downed, final boolean feeding, final String expected) {
    final PlayerClipState state = new PlayerClipState();
    state.gesture(VampirePose.RECOIL_GARLIC, 10);
    state.tick(10, false, downed, false, 0.0);
    assertEquals(
        expected,
        clip(
            state,
            10 + after,
            situation(false, false, 0.0, downed, feeding, false, Optional.empty())));
  }

  @ParameterizedTest(name = "{0} ticks after a power: {1}")
  @CsvSource({"0, power_gaze", "15, power_gaze", "16, NONE", "40, NONE"})
  @DisplayName("a power clip plays once for its own length")
  void powerPlaysOnce(final int after, final String expected) {
    final PlayerClipState state = new PlayerClipState();
    state.power(POWER, 50, LENGTHS);
    assertEquals(expected, state.powerClip(50 + after, LENGTHS).orElse("NONE"));
    assertEquals(after / OneShot.TICKS_PER_SECOND, state.powerSeconds(50 + after), 1e-9);
  }

  @ParameterizedTest(name = "levitating {0}, weakened {1}: {2}")
  @CsvSource({"false, false, VANILLA", "true, false, LEVITATE", "false, true, WEAKENED"})
  @DisplayName("a power clip plays over whatever pose the player is in, never replacing it")
  void powerLeavesThePose(final boolean levitating, final boolean weakened, final String pose) {
    final PlayerClipState state = new PlayerClipState();
    state.tick(0, levitating, false, false, 0.0);
    state.power(POWER, 1, LENGTHS);
    assertEquals(
        pose,
        clip(
            state, 2, situation(levitating, false, 0.0, false, false, weakened, Optional.empty())));
    assertEquals(Optional.of(POWER), state.powerClip(2, LENGTHS));
  }

  @ParameterizedTest(name = "second power {0} ticks after the first")
  @CsvSource({"5", "15", "16", "100"})
  @DisplayName(
      "every power used starts its clip from the first frame once, even the same clip again")
  void powerStartsFresh(final int after) {
    final PlayerClipState state = new PlayerClipState();
    state.power(POWER, 0, LENGTHS);
    assertTrue(state.takeFreshPower());
    assertFalse(state.takeFreshPower());
    state.power(POWER, after, LENGTHS);
    assertTrue(state.takeFreshPower());
    assertFalse(state.takeFreshPower());
  }

  @Test
  @DisplayName("a clip that ended unseen is not started again when the player is next drawn")
  void endedPowerIsNotFresh() {
    final PlayerClipState state = new PlayerClipState();
    state.power(POWER, 0, LENGTHS);
    assertTrue(state.powerClip(40, LENGTHS).isEmpty());
    assertFalse(state.takeFreshPower());
  }

  @ParameterizedTest(name = "{0}, riding {1}: {2}")
  @CsvSource({
    "STANDING, false, true",
    "STANDING, true, false",
    "CROUCHING, false, false",
    "SWIMMING, false, false",
    "FALL_FLYING, false, false",
    "SLEEPING, false, false"
  })
  @DisplayName("a power clip is drawn only on a player standing upright on their own feet")
  void upright(final EntityPose pose, final boolean riding, final boolean upright) {
    assertEquals(upright, PlayerClipState.upright(pose, riding));
  }

  @ParameterizedTest(name = "pose {0}, power {1}, swinging {2}, using {3}: {4}")
  @CsvSource({
    ", false, true, false, NONE",
    ", true, true, false, NONE",
    ", true, true, true, USE_ITEM_RIGHT",
    ", true, false, true, USE_ITEM_RIGHT",
    "LEVITATE, false, true, false, ATTACK",
    "LEVITATE, true, true, false, NONE",
    "LEVITATE, true, false, true, USE_ITEM_RIGHT",
    "FEED, true, true, true, NONE"
  })
  @DisplayName(
      "a swing plays over a pose that allows it but never over a power clip, which still lets an"
          + " item be held up")
  void actions(
      final VampirePose pose,
      final boolean power,
      final boolean swinging,
      final boolean using,
      final String expected) {
    assertEquals(
        expected,
        PlayerClipState.action(
                Optional.ofNullable(pose), power, Optional.empty(), swinging, using, true)
            .map(Enum::name)
            .orElse("NONE"));
  }

  @ParameterizedTest(name = "pose {0}, power {1}, swinging {2}, using {3}: {4}")
  @CsvSource({
    ", false, true, false, NONE",
    ", false, true, true, USE_ITEM_RIGHT",
    "LEVITATE, false, true, false, NONE",
    "FEED, false, true, false, NONE",
    ", true, false, true, USE_ITEM_RIGHT"
  })
  @DisplayName(
      "a thrust replaces the swing, still lets an item be held up, and lets the rig take the"
          + " player with no other clip playing")
  void actionsDuringAThrust(
      final VampirePose pose,
      final boolean power,
      final boolean swinging,
      final boolean using,
      final String expected) {
    assertEquals(
        expected,
        PlayerClipState.action(
                Optional.ofNullable(pose),
                power,
                Optional.of(VampirePose.STAKE_THRUST_RIGHT),
                swinging,
                using,
                true)
            .map(Enum::name)
            .orElse("NONE"));
  }
}
