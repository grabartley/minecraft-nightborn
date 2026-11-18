package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class StructureZoneTest {

  static Stream<StructureZone> zones() {
    return Stream.of(
        new StructureZone(
            Identifier.of("nightborn", "mountain_castle"),
            new FogZone(0.0, 64.0, 0.0, -10, 60, -10, 10, 70, 10),
            StructureAmbience.SILENT),
        new StructureZone(
            Identifier.of("nightborn", "crypt"),
            new FogZone(-1200.5, -40.0, 3000.25, -1300, -60, 2900, -1100, 20, 3100),
            new StructureAmbience(
                Optional.of(Identifier.of("nightborn", "ambient.crypt.loop")),
                Optional.empty(),
                Optional.empty())));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("zones")
  @DisplayName("a zone's area and ambience cross the network together")
  void roundTrips(final StructureZone zone) {
    final ByteBuf buf = Unpooled.buffer();
    StructureZone.PACKET_CODEC.encode(buf, zone);
    assertEquals(zone, StructureZone.PACKET_CODEC.decode(buf));
  }
}
