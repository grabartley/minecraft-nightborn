package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.nightborn.skill.PowerBar;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.codec.PacketCodecs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UsePowerPayloadTest {

  @ParameterizedTest(name = "slot {0}")
  @ValueSource(ints = {0, 2, PowerBar.SLOTS - 1})
  @DisplayName("a slot on the bar crosses the network unchanged")
  void roundTrips(final int slot) {
    final ByteBuf buf = Unpooled.buffer();
    UsePowerPayload.CODEC.encode(buf, new UsePowerPayload(slot));
    assertEquals(slot, UsePowerPayload.CODEC.decode(buf).slot());
  }

  @ParameterizedTest(name = "slot {0}")
  @ValueSource(ints = {-1, PowerBar.SLOTS, 1000})
  @DisplayName("a slot off the bar is refused while decoding")
  void rejectsOffBar(final int slot) {
    final ByteBuf buf = Unpooled.buffer();
    PacketCodecs.VAR_INT.encode(buf, slot);
    assertThrows(DecoderException.class, () -> UsePowerPayload.CODEC.decode(buf));
  }
}
