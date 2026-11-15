package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ToggleTurningBitePayloadTest {

  @ParameterizedTest(name = "{0} requests")
  @ValueSource(ints = {1, 3})
  @DisplayName("the toggle carries no data, so a client cannot claim anything with it")
  void carriesNothing(final int requests) {
    final ByteBuf buf = Unpooled.buffer();
    for (int i = 0; i < requests; i++) {
      ToggleTurningBitePayload.CODEC.encode(buf, ToggleTurningBitePayload.INSTANCE);
    }
    assertEquals(0, buf.readableBytes());
    assertEquals(ToggleTurningBitePayload.INSTANCE, ToggleTurningBitePayload.CODEC.decode(buf));
  }
}
