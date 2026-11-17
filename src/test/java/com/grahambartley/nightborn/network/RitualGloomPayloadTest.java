package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class RitualGloomPayloadTest {

  static Stream<RitualGloomPayload> glooms() {
    return Stream.of(
        new RitualGloomPayload(BlockPos.ORIGIN, 20, 8),
        new RitualGloomPayload(new BlockPos(-1200, -40, 3000), 200, 32),
        new RitualGloomPayload(new BlockPos(29_000_000, 319, -29_000_000), 1200, 128));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("glooms")
  @DisplayName("a ritual's gloom crosses the network unchanged")
  void roundTrips(final RitualGloomPayload gloom) {
    final ByteBuf buf = Unpooled.buffer();
    RitualGloomPayload.CODEC.encode(buf, gloom);
    assertEquals(gloom, RitualGloomPayload.CODEC.decode(buf));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("glooms")
  @DisplayName("only the payload sent when a ritual is cut short lifts the gloom")
  void lifts(final RitualGloomPayload gloom) {
    assertFalse(gloom.lifts());
    assertTrue(RitualGloomPayload.lifted(gloom.altar()).lifts());
    assertEquals(gloom.altar(), RitualGloomPayload.lifted(gloom.altar()).altar());
  }
}
