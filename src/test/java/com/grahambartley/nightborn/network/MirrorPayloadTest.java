package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MirrorPayloadTest {

  @ParameterizedTest(name = "entity {0}, reflects {1}")
  @CsvSource({"0, true", "42, false", "2147483647, true"})
  @DisplayName("who the mirror was raised at and whether they show cross the network unchanged")
  void roundTrips(final int entityId, final boolean reflects) {
    final ByteBuf buf = Unpooled.buffer();
    MirrorPayload.CODEC.encode(buf, new MirrorPayload(entityId, reflects));
    assertEquals(new MirrorPayload(entityId, reflects), MirrorPayload.CODEC.decode(buf));
  }
}
