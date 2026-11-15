package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VomitPayloadTest {

  @ParameterizedTest(name = "entity {0}")
  @ValueSource(ints = {0, 42, Integer.MAX_VALUE})
  @DisplayName("the vomiting entity crosses the network unchanged")
  void roundTrips(final int entityId) {
    final ByteBuf buf = Unpooled.buffer();
    VomitPayload.CODEC.encode(buf, new VomitPayload(entityId));
    assertEquals(entityId, VomitPayload.CODEC.decode(buf).entityId());
  }
}
