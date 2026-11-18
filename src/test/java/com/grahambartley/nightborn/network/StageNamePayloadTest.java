package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StageNamePayloadTest {

  @ParameterizedTest(name = "entity {0}, stage {1}, rank {2}, title {3}")
  @CsvSource({
    "0, nightborn:fledgling,,",
    "42, nightborn:ancient, nightborn:veteran,",
    "2147483647, other:stage, other:rank, other:title",
    "7, , nightborn:initiate, nightborn:never_bitten",
    "8, ,, nightborn:the_cabinet",
    "9, ,,"
  })
  @DisplayName("the bearer, the stage, the rank mark and the title, or none, cross unchanged")
  void roundTrips(final int entityId, final String stage, final String rank, final String title) {
    final StageNamePayload payload =
        new StageNamePayload(
            entityId,
            Optional.ofNullable(stage).map(Identifier::of),
            Optional.ofNullable(rank).map(Identifier::of),
            Optional.ofNullable(title).map(Identifier::of));
    final ByteBuf buf = Unpooled.buffer();
    StageNamePayload.CODEC.encode(buf, payload);
    assertEquals(payload, StageNamePayload.CODEC.decode(buf));
  }
}
