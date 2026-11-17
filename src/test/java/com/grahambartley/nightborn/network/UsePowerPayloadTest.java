package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.nightborn.skill.PowerBar;
import com.grahambartley.nightborn.skill.PowerHeading;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.codec.PacketCodecs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class UsePowerPayloadTest {

  @ParameterizedTest(name = "slot {0}, heading {1}")
  @CsvSource({"0, NONE", "2, LEFT", "4, FORWARD", "1, BACK", "3, RIGHT"})
  @DisplayName("a slot on the bar and the way the player is moving cross the network unchanged")
  void roundTrips(final int slot, final PowerHeading heading) {
    final ByteBuf buf = Unpooled.buffer();
    final UsePowerPayload sent = new UsePowerPayload(slot, heading);
    UsePowerPayload.CODEC.encode(buf, sent);
    assertEquals(sent, UsePowerPayload.CODEC.decode(buf));
  }

  @ParameterizedTest(name = "slot {0}")
  @ValueSource(ints = {-1, PowerBar.SLOTS, 1000})
  @DisplayName("a slot off the bar is refused while decoding")
  void rejectsOffBar(final int slot) {
    final ByteBuf buf = Unpooled.buffer();
    PacketCodecs.VAR_INT.encode(buf, slot);
    PowerHeading.PACKET_CODEC.encode(buf, PowerHeading.NONE);
    assertThrows(DecoderException.class, () -> UsePowerPayload.CODEC.decode(buf));
  }
}
