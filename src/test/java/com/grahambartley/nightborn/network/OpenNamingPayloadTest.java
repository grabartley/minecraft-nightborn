package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.nightborn.name.NameRules;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.codec.PacketCodecs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OpenNamingPayloadTest {

  @ParameterizedTest(name = "{0} {1}")
  @CsvSource({"7, Hester", "123456, Old Tobias", "0, Пётр"})
  @DisplayName("an entity id and a name cross the network unchanged")
  void roundTrips(final int id, final String name) {
    final OpenNamingPayload payload = new OpenNamingPayload(id, name);
    final ByteBuf buf = Unpooled.buffer();
    OpenNamingPayload.CODEC.encode(buf, payload);
    assertEquals(payload, OpenNamingPayload.CODEC.decode(buf));
  }

  @Test
  @DisplayName("a name longer than the size limit is refused while decoding")
  void rejectsHugeNames() {
    final ByteBuf buf = Unpooled.buffer();
    PacketCodecs.VAR_INT.encode(buf, 1);
    PacketCodecs.STRING.encode(buf, "a".repeat(NameRules.MAX_SENT_LENGTH + 1));
    assertThrows(DecoderException.class, () -> OpenNamingPayload.CODEC.decode(buf));
  }
}
