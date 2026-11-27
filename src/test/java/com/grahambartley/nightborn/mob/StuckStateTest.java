package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.grahambartley.nightborn.mob.stage.StageStuck;
import java.util.UUID;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StuckStateTest {

  private static final UUID QUARRY = new UUID(1L, 2L);
  private static final UUID OTHER = new UUID(3L, 4L);
  private static final Vec3d HERE = new Vec3d(0.0, 64.0, 0.0);

  private static StageStuck habits(final int ticks) {
    return new StageStuck(ticks, 0.5, 2.0, 1.5, 3, 1, 3, 10, 6);
  }

  private static StuckState stalled(final int ticks, final int wait) {
    StuckState state = StuckState.FRESH.observe(10.0, HERE, true, habits(wait));
    for (int tick = 0; tick < ticks; tick++) {
      state = state.observe(10.0, HERE, true, habits(wait));
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
    "1, 1, LAND, true"
  })
  @DisplayName("the state reads the stage's wait into the next step")
  void step(final int ticks, final int wait, final StuckWatch.Step step, final boolean stuck) {
    final StuckState state = stalled(ticks, wait);
    assertEquals(step, state.step());
    assertEquals(stuck, state.stuck());
  }

  @ParameterizedTest(name = "wait changes to {0} mid stall")
  @CsvSource({"5", "60"})
  @DisplayName("a wait that changes mid stall does not skip a step, since the first wait holds")
  void waitHeld(final int changed) {
    StuckState state = stalled(10, 20);
    for (int tick = 10; tick < 20; tick++) {
      state = state.observe(10.0, HERE, true, habits(changed));
    }
    assertEquals(StuckWatch.Step.LAND, state.step());
  }

  @ParameterizedTest(name = "landed at {0}, now {1}: held {2}")
  @CsvSource({"100, 100, true", "100, 159, true", "100, 160, false", "100, 50, true"})
  @DisplayName("a landed mob stays on foot for its hold waits")
  void holdsDown(final long landed, final long now, final boolean held) {
    assertEquals(held, stalled(20, 20).landed(landed).holdsDown(now));
  }

  @ParameterizedTest(name = "now {0}: backing {1}")
  @CsvSource({"100, true", "119, true", "120, false"})
  @DisplayName("a mob steps back to its spot for its step back waits, and stays on foot meanwhile")
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

  @ParameterizedTest(name = "backed off {0}, still pursuing {1}")
  @CsvSource({"true, true", "false, false"})
  @DisplayName("stepping back ends when the step is done or the chase is over")
  void backingEnds(final boolean backedOff, final boolean pursuing) {
    StuckState state = stalled(40, 20).steppingBack(new Vec3d(3.0, 64.0, -2.0), 100L);
    state = backedOff ? state.backedOff() : state.observe(10.0, HERE, pursuing, habits(20));
    assertNull(state.backingTo(101L));
  }

  @ParameterizedTest(name = "now {0}, same quarry {1}: forgoes {2}")
  @CsvSource({"100, true, true", "299, true, true", "300, true, false", "150, false, false"})
  @DisplayName("a mob that gives up leaves that quarry alone for its forgo waits")
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
