package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChooseWheelPayloadTest {

  @ParameterizedTest(name = "{0} {1} {2}")
  @CsvSource({
    "7, nightborn:thrall_order, 1",
    "123456, nightborn:scout, -1",
    "0, nightborn:guild_gear, 15"
  })
  @DisplayName("a chosen entity, action and argument cross the network unchanged")
  void roundTrips(final int entity, final String action, final int argument) {
    final ChooseWheelPayload payload =
        new ChooseWheelPayload(entity, Identifier.of(action), argument);
    final ByteBuf buf = Unpooled.buffer();
    ChooseWheelPayload.CODEC.encode(buf, payload);
    assertEquals(payload, ChooseWheelPayload.CODEC.decode(buf));
  }
}
