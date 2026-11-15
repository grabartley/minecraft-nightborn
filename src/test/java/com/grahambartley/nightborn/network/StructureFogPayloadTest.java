package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.structure.FogZone;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class StructureFogPayloadTest {

  static Stream<Optional<FogZone>> zones() {
    return Stream.of(
        Optional.empty(),
        Optional.of(new FogZone(0.0, 64.0, 0.0, 40.0)),
        Optional.of(new FogZone(-1200.5, -40.0, 3000.25, 150.0)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("zones")
  @DisplayName("a zone, or the lack of one, crosses the network unchanged")
  void roundTrips(final Optional<FogZone> zone) {
    final ByteBuf buf = Unpooled.buffer();
    StructureFogPayload.CODEC.encode(buf, new StructureFogPayload(zone));
    assertEquals(new StructureFogPayload(zone), StructureFogPayload.CODEC.decode(buf));
  }
}
