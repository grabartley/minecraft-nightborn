package com.grahambartley.nightborn.batform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.Unpooled;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WingedStateTest {

  @ParameterizedTest(name = "until {0}, now {1}: over {2}")
  @CsvSource({"400, 399, false", "400, 400, true"})
  @DisplayName("the winged form lasts until its time and survives a sync")
  void over(final long until, final long now, final boolean over) {
    final WingedState state = new WingedState(until, 0.5F, 2.5F, List.of("holy_water"));
    assertEquals(over, state.over(now));
    final var buf = Unpooled.buffer();
    WingedState.PACKET_CODEC.encode(buf, state);
    assertEquals(state, WingedState.PACKET_CODEC.decode(buf));
  }
}
