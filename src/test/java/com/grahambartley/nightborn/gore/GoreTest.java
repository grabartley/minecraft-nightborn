package com.grahambartley.nightborn.gore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.codec.PacketCodecs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class GoreTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(Gore.class)
  @DisplayName("every gore kind crosses the network unchanged")
  void roundTrips(final Gore gore) {
    final ByteBuf buf = Unpooled.buffer();
    Gore.PACKET_CODEC.encode(buf, gore);
    assertEquals(gore, Gore.PACKET_CODEC.decode(buf));
  }

  @ParameterizedTest(name = "index {0} -> {1}")
  @CsvSource({"-1, BITE", "6, BITE", "99, BITE"})
  @DisplayName("an index no kind owns falls back to the first kind instead of failing")
  void unknownIndex(final int index, final Gore expected) {
    final ByteBuf buf = Unpooled.buffer();
    PacketCodecs.VAR_INT.encode(buf, index);
    assertEquals(expected, Gore.PACKET_CODEC.decode(buf));
  }
}
