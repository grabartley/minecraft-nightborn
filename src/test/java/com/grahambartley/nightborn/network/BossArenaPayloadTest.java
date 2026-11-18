package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.boss.Arena;
import com.grahambartley.nightborn.boss.ArenaWall;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
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
        BossArenaPayload.left(7),
        new BossArenaPayload(0, arena, false, Optional.empty(), Optional.empty()),
        new BossArenaPayload(1, arena, false, music, Optional.empty()),
        new BossArenaPayload(2147483647, arena, true, music, Optional.empty()),
        new BossArenaPayload(
            12,
            Optional.of(Arena.room(new BlockPos(29_000_000, 319, -29_000_000), 48)),
            true,
            music,
            Optional.empty()),
        new BossArenaPayload(
            3,
            arena,
            true,
            music,
            Optional.of(
                new ArenaWall(
                    Identifier.of("nightborn", "textures/entity/boss/arena_light.png"),
                    Identifier.of("nightborn", "textures/entity/boss/arena_light_shapes.png"),
                    List.of(0xFFF6DC, 0xE8D9A8),
                    120,
                    true))));
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
  @DisplayName("leaving names the boss and carries no arena, no fight, no music and no wall")
  void left() {
    assertEquals(
        new BossArenaPayload(7, Optional.empty(), false, Optional.empty(), Optional.empty()),
        BossArenaPayload.left(7));
  }
}
