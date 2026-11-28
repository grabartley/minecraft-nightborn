package com.grahambartley.nightborn.dismember;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class LostPartTest {

  static Stream<LostPart> parts() {
    return Stream.of(
        LostPart.missing(0L, 0),
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
  @DisplayName("a newly cut part is missing from that tick with its bleed time")
  void missing() {
    assertEquals(
        new LostPart(LimbCondition.MISSING, 77L, 400, Optional.empty()),
        LostPart.missing(77L, 400));
  }

  @ParameterizedTest(name = "{0}, lost at 100 for {1} ticks, now {2}: bleeding {3}")
  @CsvSource({
    "MISSING, 400, 99, false",
    "MISSING, 400, 100, true",
    "MISSING, 400, 499, true",
    "MISSING, 400, 500, false",
    "MISSING, 0, 100, false",
    "REPLACED, 400, 200, false"
  })
  @DisplayName("a missing part bleeds from the cut until its bleed time runs out")
  void bleeding(
      final LimbCondition condition, final int bleedTicks, final long now, final boolean bleeds) {
    assertEquals(bleeds, new LostPart(condition, 100L, bleedTicks, Optional.empty()).bleeding(now));
  }

  @ParameterizedTest(name = "closed at {0} leaves {1} bleed ticks")
  @CsvSource({"100, 0", "250, 150", "499, 399"})
  @DisplayName("closing a bleeding wound ends its bleed at that tick and keeps the rest")
  void closed(final long now, final int left) {
    final LostPart open = new LostPart(LimbCondition.MISSING, 100L, 400, Optional.of(9));
    final LostPart shut = open.closed(now);
    assertEquals(new LostPart(LimbCondition.MISSING, 100L, left, Optional.of(9)), shut);
    assertFalse(shut.bleeding(now));
  }

  @ParameterizedTest(name = "now {0}")
  @CsvSource({"500", "9000"})
  @DisplayName("closing a wound that already stopped changes nothing")
  void closedAlready(final long now) {
    final LostPart done = LostPart.missing(100L, 400);
    assertSame(done, done.closed(now));
  }
}
