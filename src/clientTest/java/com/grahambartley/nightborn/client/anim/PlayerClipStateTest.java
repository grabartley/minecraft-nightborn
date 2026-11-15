package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PlayerClipStateTest {

  static final double LAND = 0.7;
  static final double VOMIT = 2.6;

  private static String clip(
      final PlayerClipState state, final double age, final boolean levitating) {
    return state.clip(age, levitating, false, 0.0, LAND, VOMIT).map(Enum::name).orElse("VANILLA");
  }

  @ParameterizedTest(name = "{0} ticks after landing: {1}")
  @CsvSource({"0, LAND", "13, LAND", "14, VANILLA", "40, VANILLA"})
  @DisplayName("landing plays its clip once, then the player is drawn as vanilla draws it")
  void lands(final int after, final String expected) {
    final PlayerClipState state = new PlayerClipState();
    state.tick(10, true);
    state.tick(11, false);
    assertEquals(expected, clip(state, 11 + after, false));
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
    state.tick(0, true);
    assertEquals(
        expected, state.clip(1, true, sprinting, speed, LAND, VOMIT).map(Enum::name).orElseThrow());
  }

  @ParameterizedTest(name = "{0} ticks after retching, levitating {1}: {2}")
  @CsvSource({"0, false, VOMIT", "51, true, VOMIT", "52, true, LEVITATE", "52, false, VANILLA"})
  @DisplayName("vomiting wins over every other clip until it ends")
  void vomits(final int after, final boolean levitating, final String expected) {
    final PlayerClipState state = new PlayerClipState();
    state.vomit(100);
    assertEquals(expected, clip(state, 100 + after, levitating));
    assertEquals(
        after / PlayerClipState.TICKS_PER_SECOND, state.vomitingSeconds(100 + after), 1e-9);
  }

  @Test
  @DisplayName("a player who never levitated or retched is drawn as vanilla draws it")
  void vanilla() {
    final PlayerClipState state = new PlayerClipState();
    state.tick(5, false);
    assertEquals("VANILLA", clip(state, 6, false));
    assertTrue(Double.isNaN(state.vomitingSeconds(6)));
  }
}
