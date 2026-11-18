package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MistStateTest {

  @ParameterizedTest(name = "until {0}, now {1}: over {2}")
  @CsvSource({"160, 159, false", "160, 160, true"})
  @DisplayName("mist lasts until its time and survives a sync")
  void over(final long until, final long now, final boolean over) {
    final MistState state = new MistState(until, 2.0F);
    assertEquals(over, state.over(now));
    final var buf = Unpooled.buffer();
    MistState.PACKET_CODEC.encode(buf, state);
    assertEquals(state, MistState.PACKET_CODEC.decode(buf));
  }
}
