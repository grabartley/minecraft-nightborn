package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StuckStateTest {

  private static final UUID QUARRY = new UUID(1L, 2L);
  private static final UUID OTHER = new UUID(3L, 4L);

  private static StuckState stalled(final int ticks, final int patience) {
    StuckState state = StuckState.FRESH.observe(10.0, true, patience);
    for (int tick = 0; tick < ticks; tick++) {
      state = state.observe(10.0, true, patience);
    }
    return state;
  }

  @ParameterizedTest(name = "stalled {0} of {1}: {2}, stuck {3}")
  @CsvSource({
    "0, 20, NONE, false",
    "19, 20, NONE, false",
    "20, 20, LAND, true",
    "40, 20, STEP_BACK, true",
    "10, 10, LAND, true",
    "1, 0, LAND, true"
  })
  @DisplayName("the state reads the stage's patience into the next step")
  void step(final int ticks, final int patience, final StuckWatch.Step step, final boolean stuck) {
    final StuckState state = stalled(ticks, patience);
    assertEquals(step, state.step());
    assertEquals(stuck, state.stuck());
  }

  @ParameterizedTest(name = "landed at {0}, now {1}: held {2}")
  @CsvSource({"100, 100, true", "100, 159, true", "100, 160, false", "100, 50, true"})
  @DisplayName("a landed mob stays on foot for three times its patience")
  void holdsDown(final long landed, final long now, final boolean held) {
    assertEquals(held, stalled(20, 20).landed(landed).holdsDown(now));
  }

  @ParameterizedTest(name = "now {0}: backing {1}")
  @CsvSource({"100, true", "119, true", "120, false"})
  @DisplayName("a mob steps back to its spot for one patience, and stays on foot meanwhile")
  void steppingBack(final long now, final boolean backing) {
    final Vec3d spot = new Vec3d(3.0, 64.0, -2.0);
    final StuckState state = stalled(40, 20).steppingBack(spot, 100L);
    assertEquals(backing ? spot : null, state.backingTo(now));
    assertEquals(1, state.watch().reroutes());
    assertEquals(0, state.watch().stalled());
    assertEquals(true, state.holdsDown(now));
  }

  @Test
  @DisplayName("with no spot to step back to, the mob still counts the try and stays on foot")
  void noSpot() {
    final StuckState state = stalled(40, 20).steppingBack(null, 100L);
    assertNull(state.backingTo(100L));
    assertEquals(1, state.watch().reroutes());
    assertEquals(true, state.holdsDown(100L));
  }

  @ParameterizedTest(name = "now {0}, same quarry {1}: forgoes {2}")
  @CsvSource({"100, true, true", "299, true, true", "300, true, false", "150, false, false"})
  @DisplayName("a mob that gives up leaves that quarry alone for ten times its patience")
  void forgoes(final long now, final boolean same, final boolean forgoes) {
    final StuckState state = stalled(40, 20).gaveUp(QUARRY, 100L);
    assertEquals(forgoes, state.forgoes(same ? QUARRY : OTHER, now));
    assertEquals(StuckWatch.FRESH, state.watch());
  }

  @Test
  @DisplayName("a fresh mob is not stuck, held down, backing off or ignoring anyone")
  void fresh() {
    assertEquals(false, StuckState.FRESH.stuck());
    assertEquals(false, StuckState.FRESH.holdsDown(0L));
    assertNull(StuckState.FRESH.backingTo(0L));
    assertEquals(false, StuckState.FRESH.forgoes(QUARRY, 0L));
  }
}
