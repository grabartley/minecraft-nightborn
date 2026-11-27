package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.mob.VampirePose;
import java.util.Optional;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ThrustStateTest {

  static final PlayerClipState.Lengths LENGTHS =
      animation -> animation.startsWith("stake_thrust") ? 0.45 : 0.0;

  private static ThrustState swungAt(
      final int age, final boolean reverseGrip, final boolean rightArm) {
    final ThrustState state = new ThrustState();
    state.swing(false, 0, reverseGrip, rightArm, age - 1, LENGTHS);
    state.swing(true, 0, reverseGrip, rightArm, age, LENGTHS);
    return state;
  }

  private static String thrust(final ThrustState state, final double age) {
    return state.playing(age, LENGTHS).map(Enum::name).orElse("NONE");
  }

  @ParameterizedTest(name = "{0} ticks after the swing: {1}")
  @CsvSource({
    "0, STAKE_THRUST_RIGHT",
    "8, STAKE_THRUST_RIGHT",
    "8.9, STAKE_THRUST_RIGHT",
    "9, NONE",
    "20, NONE"
  })
  @DisplayName("a swing with a reverse grip item plays the thrust once for its own length")
  void thrustPlaysOnce(final double after, final String expected) {
    final ThrustState state = swungAt(10, true, true);
    assertEquals(expected, thrust(state, 10 + after));
  }

  @ParameterizedTest(name = "right arm {0}: {1}")
  @CsvSource({"true, STAKE_THRUST_RIGHT", "false, STAKE_THRUST_LEFT"})
  @DisplayName("the thrust plays on the arm that swung")
  void thrustArm(final boolean rightArm, final String expected) {
    assertEquals(expected, thrust(swungAt(10, true, rightArm), 11));
  }

  @Test
  @DisplayName("a swing with any other item plays no thrust")
  void otherItemsSwing() {
    assertEquals("NONE", thrust(swungAt(10, false, true), 11));
  }

  @Test
  @DisplayName("a swing with another item ends a thrust that is still playing")
  void otherSwingEndsTheThrust() {
    final ThrustState state = swungAt(10, true, true);
    state.swing(false, 0, false, true, 13, LENGTHS);
    state.swing(true, 0, false, true, 14, LENGTHS);
    assertEquals("NONE", thrust(state, 15));
  }

  @Test
  @DisplayName("an arm still mid swing does not start the thrust again")
  void swingInProgress() {
    final ThrustState state = swungAt(10, true, true);
    assertTrue(state.takeFresh());
    state.swing(true, 1, true, true, 11, LENGTHS);
    state.swing(true, 2, true, true, 12, LENGTHS);
    assertFalse(state.takeFresh());
    assertEquals(2 / OneShot.TICKS_PER_SECOND, state.seconds(12), 1e-9);
  }

  @Test
  @DisplayName(
      "a swing that restarts while the thrust plays, as when mining, lets the thrust finish first")
  void restartedSwing() {
    final ThrustState state = swungAt(10, true, true);
    state.takeFresh();
    state.swing(true, 3, true, true, 13, LENGTHS);
    state.swing(true, 0, true, true, 14, LENGTHS);
    assertFalse(state.takeFresh());
    assertEquals(4 / OneShot.TICKS_PER_SECOND, state.seconds(14), 1e-9);
    assertEquals("NONE", thrust(state, 19));
    state.swing(true, 3, true, true, 19, LENGTHS);
    state.swing(true, 0, true, true, 20, LENGTHS);
    assertTrue(state.takeFresh());
    assertEquals("STAKE_THRUST_RIGHT", thrust(state, 28));
  }

  @ParameterizedTest(name = "pose {0}, power {1}: {2}")
  @CsvSource({
    ", false, STAKE_THRUST_RIGHT",
    "LEVITATE, false, STAKE_THRUST_RIGHT",
    "SUN_BURN, false, STAKE_THRUST_RIGHT",
    "FEED, false, NONE",
    "DOWNED, false, NONE",
    "VOMIT, false, NONE",
    ", true, NONE",
    "LEVITATE, true, NONE"
  })
  @DisplayName(
      "a thrust shows only over a pose that allows actions, and a power clip outranks it in"
          + " every view")
  void shown(final VampirePose pose, final boolean power, final String expected) {
    assertEquals(
        expected,
        ThrustState.shown(
                Optional.ofNullable(pose), power, Optional.of(VampirePose.STAKE_THRUST_RIGHT))
            .map(Enum::name)
            .orElse("NONE"));
    assertTrue(ThrustState.shown(Optional.ofNullable(pose), power, Optional.empty()).isEmpty());
  }

  @ParameterizedTest(name = "{0} hand, main arm {1}: right arm {2}")
  @CsvSource({
    "MAIN_HAND, RIGHT, true",
    "OFF_HAND, RIGHT, false",
    "MAIN_HAND, LEFT, false",
    "OFF_HAND, LEFT, true",
    ", RIGHT, true",
    ", LEFT, false"
  })
  @DisplayName("the thrust plays on the arm of the hand that swung, the main hand when unknown")
  void swingsRightArm(final Hand hand, final Arm mainArm, final boolean right) {
    assertEquals(right, ThrustState.swingsRightArm(hand, mainArm));
  }

  @Test
  @DisplayName("a thrust that ended unseen is not started again when the player is next drawn")
  void endedThrustIsNotFresh() {
    final ThrustState state = swungAt(10, true, true);
    assertEquals("NONE", thrust(state, 30));
    assertFalse(state.takeFresh());
  }

  @Test
  @DisplayName("no thrust plays before the first swing")
  void noSwingNoThrust() {
    final ThrustState state = new ThrustState();
    assertEquals("NONE", thrust(state, 0));
    assertFalse(state.takeFresh());
  }

  @Test
  @DisplayName("putting the stake away ends its thrust")
  void switchedAway() {
    final ThrustState state = swungAt(10, true, true);
    state.swing(true, 1, false, true, 11, LENGTHS);
    assertEquals("NONE", thrust(state, 12));
  }

  @ParameterizedTest(name = "{0} on the {1} arm: {2}")
  @CsvSource({
    "STAKE_THRUST_RIGHT, RIGHT, true",
    "STAKE_THRUST_RIGHT, LEFT, false",
    "STAKE_THRUST_LEFT, LEFT, true",
    "STAKE_THRUST_LEFT, RIGHT, false"
  })
  @DisplayName("only the hand that thrusts follows the thrust in first person")
  void sways(final VampirePose pose, final Arm arm, final boolean sways) {
    assertEquals(sways, ThrustState.sways(pose, arm));
  }

  @ParameterizedTest(name = "{0}s into a 0.45s thrust: {1}")
  @CsvSource({
    "0, 0",
    "0.025, 0.5",
    "0.05, 1",
    "0.2, 1",
    "0.3, 1",
    "0.375, 0.5",
    "0.45, 0",
    "0.5, 0"
  })
  @DisplayName(
      "the first person hand eases into the thrust and back to where vanilla holds it, so it"
          + " never snaps")
  void swayWeight(final double seconds, final double weight) {
    assertEquals(weight, ThrustState.swayWeight(seconds, 0.45), 1e-9);
  }
}
