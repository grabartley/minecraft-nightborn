package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PlayerCuePayloadTest {

  @ParameterizedTest(name = "entity {0}, {1}")
  @CsvSource({"0, VOMIT", "42, DRAIN", "2147483647, VOMIT", "7, DRAIN", "9, FLINCH"})
  @DisplayName("the player and the cue cross the network unchanged")
  void roundTrips(final int entityId, final PlayerCue cue) {
    final ByteBuf buf = Unpooled.buffer();
    PlayerCuePayload.CODEC.encode(buf, new PlayerCuePayload(entityId, cue));
    assertEquals(new PlayerCuePayload(entityId, cue), PlayerCuePayload.CODEC.decode(buf));
  }
}
