package com.grahambartley.nightborn.dismember;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class LostPartTest {

  static Stream<LostPart> parts() {
    return Stream.of(
        LostPart.missing(0L),
        new LostPart(LimbCondition.MISSING, 48_000L, 200, Optional.of(24_000)),
        new LostPart(LimbCondition.REPLACED, 9L, 0, Optional.empty()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("parts")
  @DisplayName("a lost part is saved and read back unchanged")
  void saved(final LostPart part) {
    assertEquals(
        part,
        LostPart.CODEC
            .parse(
                JsonOps.INSTANCE, LostPart.CODEC.encodeStart(JsonOps.INSTANCE, part).getOrThrow())
            .getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("parts")
  @DisplayName("a lost part crosses the network unchanged")
  void synced(final LostPart part) {
    final PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
    LostPart.PACKET_CODEC.encode(buf, part);
    assertEquals(part, LostPart.PACKET_CODEC.decode(buf));
  }

  @Test
  @DisplayName("a newly cut part is missing from that tick with no timers yet")
  void missing() {
    assertEquals(
        new LostPart(LimbCondition.MISSING, 77L, 0, Optional.empty()), LostPart.missing(77L));
  }
}
