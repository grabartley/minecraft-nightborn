package com.grahambartley.nightborn.vampire;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VisibleStateTest {

  @ParameterizedTest(name = "weakened {0} -> {1}")
  @CsvSource({"-5, 0", "0, 0", "40, 40", "100, 100", "400, 100"})
  @DisplayName("the weakened level shown to others is kept within the cap")
  void clampsWeakened(final int given, final int kept) {
    final VisibleState state = new VisibleState(false, false, false, given);
    assertEquals(kept, state.weakened());
    assertEquals(kept > 0, state.isWeakened());
  }

  @ParameterizedTest(name = "eyes {0}, feeding {1}, downed {2}, weakened {3}")
  @CsvSource({
    "true, false, false, 0",
    "true, true, false, 0",
    "false, false, true, 100",
    "true, true, true, 35"
  })
  @DisplayName("what others can see travels to their clients intact")
  void packetRoundTrip(
      final boolean eyes, final boolean feeding, final boolean downed, final int weakened) {
    final VisibleState state = new VisibleState(eyes, feeding, downed, weakened);
    final ByteBuf buf = Unpooled.buffer();
    VisibleState.PACKET_CODEC.encode(buf, state);
    assertEquals(state, VisibleState.PACKET_CODEC.decode(buf));
  }
}
