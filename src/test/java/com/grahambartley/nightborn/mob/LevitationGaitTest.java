package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LevitationGaitTest {

  private static final int SETTLED_TICKS = 60;

  private static LevitationGait settled(final boolean sprinting, final double speed) {
    final LevitationGait gait = new LevitationGait();
    for (int tick = 0; tick < SETTLED_TICKS; tick++) {
      gait.observe(tick, true, sprinting, speed);
    }
    return gait;
  }

  private static List<VampirePose> run(final LevitationGait gait, final double... speeds) {
    final List<VampirePose> shown = new ArrayList<>();
    for (int tick = 0; tick < speeds.length; tick++) {
      gait.observe(SETTLED_TICKS + tick, true, false, speeds[tick]);
      shown.add(gait.pose());
    }
    return shown;
  }

  private static long changes(final List<VampirePose> shown) {
    long changes = 0;
    for (int i = 1; i < shown.size(); i++) {
      if (shown.get(i) != shown.get(i - 1)) {
        changes++;
      }
    }
    return changes;
  }

  @ParameterizedTest(name = "speed {0}, sprinting {1}: {2}")
  @CsvSource({
    "0.0, false, LEVITATE",
    "0.015, false, LEVITATE",
    "0.039, false, LEVITATE",
    "0.05, false, LEVITATE_MOVE",
    "0.1, false, LEVITATE_MOVE",
    "0.25, false, LEVITATE_MOVE",
    "0.27, false, LEVITATE_SPRINT",
    "0.5, false, LEVITATE_SPRINT",
    "0.0, true, LEVITATE_SPRINT",
    "0.1, true, LEVITATE_SPRINT"
  })
  @DisplayName("a steady speed settles on the hover, move or sprint clip")
  void settles(final double speed, final boolean sprinting, final VampirePose pose) {
    assertEquals(pose, settled(sprinting, speed).pose());
  }

  @ParameterizedTest(name = "shown {0}, sprinting {1}, speed {2}: {3}")
  @CsvSource({
    "LEVITATE, false, 0.03, LEVITATE",
    "LEVITATE, false, 0.04, LEVITATE",
    "LEVITATE, false, 0.041, LEVITATE_MOVE",
    "LEVITATE_MOVE, false, 0.03, LEVITATE_MOVE",
    "LEVITATE_MOVE, false, 0.021, LEVITATE_MOVE",
    "LEVITATE_MOVE, false, 0.02, LEVITATE",
    "LEVITATE_MOVE, false, 0.24, LEVITATE_MOVE",
    "LEVITATE_MOVE, false, 0.26, LEVITATE_MOVE",
    "LEVITATE_MOVE, false, 0.261, LEVITATE_SPRINT",
    "LEVITATE_SPRINT, false, 0.24, LEVITATE_SPRINT",
    "LEVITATE_SPRINT, false, 0.201, LEVITATE_SPRINT",
    "LEVITATE_SPRINT, false, 0.2, LEVITATE_MOVE",
    "LEVITATE_SPRINT, false, 0.01, LEVITATE",
    "LEVITATE, true, 0.0, LEVITATE_SPRINT",
    "LEVITATE_MOVE, true, 0.0, LEVITATE_SPRINT"
  })
  @DisplayName("each threshold has a margin, so a speed just past it keeps the clip shown")
  void margins(
      final VampirePose shown,
      final boolean sprinting,
      final double speed,
      final VampirePose pose) {
    assertEquals(pose, LevitationGait.wanted(shown, sprinting, speed));
  }

  @ParameterizedTest(name = "speeds {0} and {1} every {2} ticks")
  @CsvSource({
    "0.0248, 0.1044, 3",
    "0.02, 0.06, 3",
    "0.0, 0.12, 2",
    "0.2, 0.33, 3",
    "0.0178, 0.0627, 3"
  })
  @DisplayName(
      "speed that pulses in steps, as a mob's position reaches the client, never flips the clip"
          + " back and forth")
  void pulsesHoldOneClip(final double low, final double high, final int every) {
    final LevitationGait gait = settled(false, (low + high) / 2.0);
    final double[] speeds = new double[200];
    for (int tick = 0; tick < speeds.length; tick++) {
      speeds[tick] = (tick / every) % 2 == 0 ? low : high;
    }
    final long changes = changes(run(gait, speeds));
    assertTrue(changes <= 1, () -> "changes: " + changes);
  }

  @ParameterizedTest(name = "sprint flag flips every {0} ticks")
  @CsvSource({"1", "2"})
  @DisplayName("a sprint flag that flickers for a tick or two never flips the clip")
  void flickeringSprintHoldsOneClip(final int every) {
    final LevitationGait gait = settled(false, 0.1);
    final List<VampirePose> shown = new ArrayList<>();
    for (int tick = 0; tick < 200; tick++) {
      gait.observe(SETTLED_TICKS + tick, true, (tick / every) % 2 == 1, 0.1);
      shown.add(gait.pose());
    }
    assertEquals(0, changes(shown));
    assertEquals(VampirePose.LEVITATE_MOVE, gait.pose());
  }

  @ParameterizedTest(name = "{0} ticks after a change, every tick seen {1}: {2}")
  @CsvSource({
    "3, true, LEVITATE_MOVE",
    "9, true, LEVITATE_MOVE",
    "10, true, LEVITATE_SPRINT",
    "3, false, LEVITATE_MOVE",
    "9, false, LEVITATE_MOVE",
    "10, false, LEVITATE_SPRINT"
  })
  @DisplayName("the hold is counted in game ticks, whether or not every tick was seen")
  void holdCountsGameTicks(final int after, final boolean everyTick, final VampirePose pose) {
    final LevitationGait gait = settled(false, 0.0);
    gait.observe(SETTLED_TICKS, true, false, 1.0);
    assertEquals(VampirePose.LEVITATE_MOVE, gait.pose());
    for (int tick = everyTick ? 1 : after; tick <= after; tick++) {
      gait.observe(SETTLED_TICKS + tick, true, false, 1.0);
    }
    assertEquals(pose, gait.pose());
  }

  @Test
  @DisplayName("after a long gap the speed starts again from the fresh sample, not the old one")
  void staleSpeedRestarts() {
    final LevitationGait gait = settled(false, 0.5);
    gait.observe(SETTLED_TICKS + LevitationGait.STALE_TICKS + 1, true, false, 0.0);
    assertEquals(0.0, gait.spread(), 1.0e-9);
  }

  @ParameterizedTest(name = "observed {0} times in one tick")
  @CsvSource({"1", "3", "10"})
  @DisplayName("several frames in one tick count as one sample")
  void oneSamplePerTick(final int frames) {
    final LevitationGait gait = new LevitationGait();
    gait.observe(6, true, false, 0.0);
    for (int frame = 0; frame < frames; frame++) {
      gait.observe(7, true, false, 0.24);
    }
    assertEquals(0.25, gait.spread(), 1.0e-9);
  }

  @ParameterizedTest(name = "speed {0}, sprinting {1}: spread {2}")
  @CsvSource({
    "0.0, false, 0.0",
    "0.06, false, 0.25",
    "0.12, false, 0.5",
    "0.24, false, 1.0",
    "0.5, false, 1.0",
    "-0.1, false, 0.0",
    "0.0, true, 1.0"
  })
  @DisplayName("arms spread with the settled speed until a sprint, where they are fully out")
  void spread(final double speed, final boolean sprinting, final double spread) {
    assertEquals(spread, settled(sprinting, speed).spread(), 1.0e-6);
  }

  @ParameterizedTest(name = "speed {0}: {1}")
  @CsvSource({"0.0, LEVITATE", "0.1, LEVITATE_MOVE", "0.5, LEVITATE_SPRINT"})
  @DisplayName("on the ground the clip follows speed at once, so lifting off shows the right one")
  void groundedFollowsAtOnce(final double speed, final VampirePose pose) {
    final LevitationGait gait = new LevitationGait();
    for (int tick = 0; tick < 60; tick++) {
      gait.observe(tick, false, false, speed);
    }
    gait.observe(60, true, false, speed);
    assertEquals(pose, gait.pose());
  }
}
