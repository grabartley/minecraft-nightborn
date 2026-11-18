package com.grahambartley.nightborn.client.moment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.moment.Moment;
import com.grahambartley.nightborn.moment.MomentState;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class CeremonyFadeTest {

  private static MomentState state(final Moment moment) {
    return new MomentState(moment, 0L, 0.0F, Vec3d.ZERO, 100, 60);
  }

  @ParameterizedTest(name = "{0} ticks in -> card {1}")
  @CsvSource({"50, 0", "100, 0", "106, 0.5", "112, 1", "130, 1", "154, 0.5", "160, 0"})
  @DisplayName("the card fades in after the camera and fades out before the moment ends")
  void card(final float elapsed, final float alpha) {
    assertEquals(alpha, CeremonyFade.card(state(Moment.OATH), elapsed), 1.0E-3F);
  }

  @ParameterizedTest(name = "{0} ticks in -> night {1}")
  @CsvSource({"0, 0", "50, 0.225", "100, 0.45", "130, 0.225", "160, 0"})
  @DisplayName("a turning sinks into the colours of night and lifts again")
  void night(final float elapsed, final float alpha) {
    assertEquals(alpha, CeremonyFade.night(state(Moment.TURNING), elapsed), 1.0E-3F);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Moment.class)
  @DisplayName("nothing on screen jumps sharply from one tick to the next, so it never flashes")
  void neverFlashes(final Moment moment) {
    final MomentState state = state(moment);
    for (int tick = 0; tick < state.totalTicks() + 5; tick++) {
      assertTrue(
          Math.abs(CeremonyFade.card(state, tick + 1) - CeremonyFade.card(state, tick)) <= 0.1F);
      assertTrue(
          Math.abs(CeremonyFade.night(state, tick + 1) - CeremonyFade.night(state, tick)) <= 0.05F);
    }
  }

  @ParameterizedTest(name = "{0} ticks in")
  @CsvSource({"0", "50", "130"})
  @DisplayName("an oath never darkens the screen")
  void oathStaysLight(final float elapsed) {
    assertEquals(0.0F, CeremonyFade.night(state(Moment.OATH), elapsed));
  }
}
