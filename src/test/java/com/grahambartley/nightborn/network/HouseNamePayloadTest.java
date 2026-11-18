package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HouseNamePayloadTest {

  @ParameterizedTest(name = "entity {0}, house {1}")
  @CsvSource({"0, Valcourt", "2147483647, House of Thorns", "5,"})
  @DisplayName("the bearer and its house name, or none, cross unchanged")
  void roundTrips(final int entityId, final String house) {
    final HouseNamePayload payload = new HouseNamePayload(entityId, Optional.ofNullable(house));
    final ByteBuf buf = Unpooled.buffer();
    HouseNamePayload.CODEC.encode(buf, payload);
    assertEquals(payload, HouseNamePayload.CODEC.decode(buf));
  }
}
