package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.Unpooled;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RevealPayloadTest {

  @ParameterizedTest(name = "entity {0} in colour {1} for {2} ticks")
  @CsvSource({"12, 9044739, 15", "0, 0, 1"})
  @DisplayName("a reveal crosses the wire unchanged")
  void roundTrips(final int id, final int colour, final int ticks) {
    final RevealPayload payload = new RevealPayload(List.of(id, id + 1), List.of(colour, 7), ticks);
    final var buf = Unpooled.buffer();
    RevealPayload.CODEC.encode(buf, payload);
    assertEquals(payload, RevealPayload.CODEC.decode(buf));
  }
}
