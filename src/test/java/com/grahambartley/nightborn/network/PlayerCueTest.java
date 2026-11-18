package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.codec.PacketCodecs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class PlayerCueTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(PlayerCue.class)
  @DisplayName("every cue crosses the network unchanged")
  void roundTrips(final PlayerCue cue) {
    final ByteBuf buf = Unpooled.buffer();
    PlayerCue.PACKET_CODEC.encode(buf, cue);
    assertEquals(cue, PlayerCue.PACKET_CODEC.decode(buf));
  }

  @ParameterizedTest(name = "index {0} -> {1}")
  @CsvSource({"-1, VOMIT", "4, VOMIT", "99, VOMIT"})
  @DisplayName("an index no cue owns falls back to the first cue instead of failing")
  void unknownIndex(final int index, final PlayerCue expected) {
    final ByteBuf buf = Unpooled.buffer();
    PacketCodecs.VAR_INT.encode(buf, index);
    assertEquals(expected, PlayerCue.PACKET_CODEC.decode(buf));
  }
}
