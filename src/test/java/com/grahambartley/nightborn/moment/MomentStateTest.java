package com.grahambartley.nightborn.moment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class MomentStateTest {

  private static final MomentState STATE = new MomentState(Moment.OATH, 1000L, 90.0F, 100, 60);

  @ParameterizedTest(name = "{0} ticks in -> {1}")
  @CsvSource({"0, CAMERA", "99.5, CAMERA", "100, CARD", "159.9, CARD", "160, OVER", "500, OVER"})
  @DisplayName("the camera circles first, then the card shows, then the moment is over")
  void phase(final float elapsed, final MomentState.Phase phase) {
    assertEquals(phase, STATE.phase(elapsed));
  }

  @ParameterizedTest(name = "{0} ticks in -> camera {1}, card {2}")
  @CsvSource({"0, 0, 0", "50, 0.5, 0", "100, 1, 0", "130, 1, 0.5", "200, 1, 1"})
  @DisplayName("progress through the camera and the card runs from none to all")
  void progress(final float elapsed, final float camera, final float card) {
    assertEquals(camera, STATE.cameraProgress(elapsed), 1.0E-4F);
    assertEquals(card, STATE.cardProgress(elapsed), 1.0E-4F);
  }

  @ParameterizedTest(name = "now {0}, delta {1} -> {2}")
  @CsvSource({"1000, 0, 0", "1010, 0.5, 10.5", "990, 0.5, 0"})
  @DisplayName("time in the moment counts from when it began and never runs backwards")
  void elapsed(final long now, final float delta, final float elapsed) {
    assertEquals(elapsed, STATE.elapsed(now, delta), 1.0E-4F);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Moment.class)
  @DisplayName("a moment reaches the client unchanged")
  void syncs(final Moment moment) {
    final MomentState state = new MomentState(moment, 123456789L, -45.5F, 100, 60);
    final ByteBuf buffer = Unpooled.buffer();
    MomentState.PACKET_CODEC.encode(buffer, state);
    assertEquals(state, MomentState.PACKET_CODEC.decode(buffer));
  }
}
