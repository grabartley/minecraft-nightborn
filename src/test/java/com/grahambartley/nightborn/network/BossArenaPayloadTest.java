package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.boss.Arena;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class BossArenaPayloadTest {

  static Stream<BossArenaPayload> payloads() {
    final Optional<Arena> arena = Optional.of(new Arena(new BlockPos(-1200, 70, 3000), 16));
    final Optional<Identifier> music = Optional.of(Identifier.of("nightborn", "music.boss.eldest"));
    return Stream.of(
        BossArenaPayload.left(),
        new BossArenaPayload(arena, false, Optional.empty()),
        new BossArenaPayload(arena, false, music),
        new BossArenaPayload(arena, true, music),
        new BossArenaPayload(
            Optional.of(new Arena(new BlockPos(29_000_000, 319, -29_000_000), 48)), true, music));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("payloads")
  @DisplayName("an arena crosses the network unchanged")
  void roundTrips(final BossArenaPayload payload) {
    final ByteBuf buf = Unpooled.buffer();
    BossArenaPayload.CODEC.encode(buf, payload);
    assertEquals(payload, BossArenaPayload.CODEC.decode(buf));
  }

  @Test
  @DisplayName("leaving carries no arena, no fight and no music")
  void left() {
    assertEquals(
        new BossArenaPayload(Optional.empty(), false, Optional.empty()), BossArenaPayload.left());
  }
}
