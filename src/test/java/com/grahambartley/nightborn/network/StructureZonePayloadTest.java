package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.structure.AmbientAdditions;
import com.grahambartley.nightborn.structure.FogZone;
import com.grahambartley.nightborn.structure.StructureAmbience;
import com.grahambartley.nightborn.structure.StructureMusic;
import com.grahambartley.nightborn.structure.StructureZone;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class StructureZonePayloadTest {

  static Stream<Optional<StructureZone>> zones() {
    return Stream.of(
        Optional.empty(),
        Optional.of(
            new StructureZone(
                Identifier.of("nightborn", "crypt"),
                new FogZone(0.0, 64.0, 0.0, -10, 60, -10, 10, 70, 10),
                StructureAmbience.SILENT)),
        Optional.of(
            new StructureZone(
                Identifier.of("nightborn", "ruined_manor"),
                new FogZone(-1200.5, -40.0, 3000.25, -1300, -60, 2900, -1100, 20, 3100),
                new StructureAmbience(
                    Optional.of(Identifier.of("nightborn", "ambient.manor.loop")),
                    Optional.of(
                        new AmbientAdditions(
                            Identifier.of("nightborn", "ambient.manor.additions"), 0.0111)),
                    Optional.of(
                        new StructureMusic(
                            Identifier.of("nightborn", "music.gothic"), 600, 2400, true))))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("zones")
  @DisplayName("a zone, or the lack of one, crosses the network unchanged")
  void roundTrips(final Optional<StructureZone> zone) {
    final ByteBuf buf = Unpooled.buffer();
    StructureZonePayload.CODEC.encode(buf, new StructureZonePayload(zone));
    assertEquals(new StructureZonePayload(zone), StructureZonePayload.CODEC.decode(buf));
  }
}
