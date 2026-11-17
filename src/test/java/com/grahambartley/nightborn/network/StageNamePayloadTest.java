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

  @ParameterizedTest(name = "entity {0}, stage {1}")
  @CsvSource({"0, nightborn:fledgling", "42, nightborn:ancient", "2147483647, other:stage", "7,"})
  @DisplayName("the bearer and the stage, or no stage, cross the network unchanged")
  void roundTrips(final int entityId, final String stage) {
    final StageNamePayload payload =
        new StageNamePayload(entityId, Optional.ofNullable(stage).map(Identifier::of));
    final ByteBuf buf = Unpooled.buffer();
    StageNamePayload.CODEC.encode(buf, payload);
    assertEquals(payload, StageNamePayload.CODEC.decode(buf));
  }
}
