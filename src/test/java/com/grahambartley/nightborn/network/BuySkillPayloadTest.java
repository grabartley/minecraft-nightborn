package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BuySkillPayloadTest {

  @Test
  @DisplayName("a skill id crosses the network unchanged")
  void roundTrips() {
    final BuySkillPayload payload =
        new BuySkillPayload(Identifier.of("nightborn", "core/thick_blood"));
    final ByteBuf buf = Unpooled.buffer();
    BuySkillPayload.CODEC.encode(buf, payload);
    assertEquals(payload, BuySkillPayload.CODEC.decode(buf));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"Not An Id", "nightborn:UPPER", "a:b:c"})
  @DisplayName("a string that is not an id is refused while decoding")
  void rejectsBadIds(final String raw) {
    final ByteBuf buf = Unpooled.buffer();
    PacketCodecs.STRING.encode(buf, raw);
    assertThrows(DecoderException.class, () -> BuySkillPayload.CODEC.decode(buf));
  }

  @Test
  @DisplayName("an id longer than the limit is refused, so a client cannot send a huge string")
  void rejectsLongIds() {
    final ByteBuf buf = Unpooled.buffer();
    PacketCodecs.STRING.encode(buf, "nightborn:" + "a".repeat(BuySkillPayload.MAX_ID_LENGTH));
    assertThrows(DecoderException.class, () -> BuySkillPayload.CODEC.decode(buf));
  }
}
