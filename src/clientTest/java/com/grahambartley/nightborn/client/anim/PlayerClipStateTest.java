package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PlayerClipStateTest {

  static final PlayerClipState.Lengths LENGTHS = new PlayerClipState.Lengths(0.7, 2.6, 1.4, 1.6);

  private static PlayerClipState.Situation still() {
    return new PlayerClipState.Situation(false, false, 0.0, false, false, false);
  }

  private static PlayerClipState.Situation levitating() {
    return new PlayerClipState.Situation(true, false, 0.0, false, false, false);
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
    state.tick(10, true, false);
    state.tick(11, false, false);
    assertEquals(expected, clip(state, 11 + after, still()));
  }

  @ParameterizedTest(name = "speed {0}, sprinting {1}: {2}")
  @CsvSource({
    "0.0, false, LEVITATE",
    "0.1, false, LEVITATE_MOVE",
    "0.1, true, LEVITATE_SPRINT",
    "0.3, false, LEVITATE_SPRINT"
  })
  @DisplayName("levitating picks the hover, move or sprint clip by speed")
  void levitates(final double speed, final boolean sprinting, final String expected) {
    final PlayerClipState state = new PlayerClipState();
    state.tick(0, true, false);
    assertEquals(
        expected,
        clip(state, 1, new PlayerClipState.Situation(true, sprinting, speed, false, false, false)));
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
    state.tick(10, false, true);
    assertEquals(
        "DOWNED",
        clip(state, 10, new PlayerClipState.Situation(false, false, 0.0, true, false, false)));
    state.tick(11, false, false);
    assertEquals(expected, clip(state, 11 + after, still()));
  }

  @Test
  @DisplayName("a player seen for the first time already standing does not play getting up")
  void noRiseWithoutDowned() {
    final PlayerClipState state = new PlayerClipState();
    state.tick(10, false, false);
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
            new PlayerClipState.Situation(false, false, 0.0, false, feeding, false)));
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
    state.tick(0, levitating, downed);
    assertEquals(
        expected,
        clip(
            state,
            1,
            new PlayerClipState.Situation(levitating, false, speed, downed, feeding, weakened)));
  }

  @Test
  @DisplayName("a player who never levitated or retched is drawn as vanilla draws it")
  void vanilla() {
    final PlayerClipState state = new PlayerClipState();
    state.tick(5, false, false);
    assertEquals("VANILLA", clip(state, 6, still()));
    assertTrue(Double.isNaN(state.vomitingSeconds(6)));
  }
}
