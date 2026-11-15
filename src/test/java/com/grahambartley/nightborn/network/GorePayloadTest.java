package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.gore.Gore;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GorePayloadTest {

  @ParameterizedTest(name = "entity {0}, {1}, yaw {2}")
  @CsvSource({"0, BITE, 0", "42, STAKED, -90", "2147483647, DRAINED, 179.5", "7, SILVER, NaN"})
  @DisplayName("the victim, the kind of gore and the spray direction cross the network unchanged")
  void roundTrips(final int entityId, final Gore gore, final float yaw) {
    final ByteBuf buf = Unpooled.buffer();
    GorePayload.CODEC.encode(buf, new GorePayload(entityId, gore, yaw));
    assertEquals(new GorePayload(entityId, gore, yaw), GorePayload.CODEC.decode(buf));
  }
}
