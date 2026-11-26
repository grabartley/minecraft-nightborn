package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PowerClipPayloadTest {

  @ParameterizedTest(name = "entity {0}, {1}")
  @CsvSource({"0, power_gaze", "42, blood_rush", "2147483647, power_set"})
  @DisplayName("the player and the power clip cross the network unchanged")
  void roundTrips(final int entityId, final String clip) {
    final ByteBuf buf = Unpooled.buffer();
    PowerClipPayload.CODEC.encode(buf, new PowerClipPayload(entityId, clip));
    assertEquals(new PowerClipPayload(entityId, clip), PowerClipPayload.CODEC.decode(buf));
  }
}
