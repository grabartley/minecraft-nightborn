package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.nightborn.skill.PowerBar;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import java.util.Optional;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SetPowerSlotPayloadTest {

  @ParameterizedTest(name = "slot {0}, skill [{1}]")
  @CsvSource({"0, nightborn:core/vanish", "4, nightborn:core/blood_rush", "2, ''"})
  @DisplayName(
      "a slot and the power put on it, or nothing to clear it, cross the network unchanged")
  void roundTrips(final int slot, final String skill) {
    final ByteBuf buf = Unpooled.buffer();
    final SetPowerSlotPayload sent =
        new SetPowerSlotPayload(
            slot, skill.isEmpty() ? Optional.empty() : Optional.of(Identifier.of(skill)));
    SetPowerSlotPayload.CODEC.encode(buf, sent);
    assertEquals(sent, SetPowerSlotPayload.CODEC.decode(buf));
  }

  @ParameterizedTest(name = "slot {0}")
  @ValueSource(ints = {-1, PowerBar.SLOTS, 1000})
  @DisplayName("a slot off the bar is refused while decoding")
  void rejectsOffBar(final int slot) {
    final ByteBuf buf = Unpooled.buffer();
    PacketCodecs.VAR_INT.encode(buf, slot);
    PacketCodecs.BOOL.encode(buf, false);
    assertThrows(DecoderException.class, () -> SetPowerSlotPayload.CODEC.decode(buf));
  }

  @ParameterizedTest(name = "[{0}]")
  @ValueSource(strings = {"Not An Id", "a:b:c"})
  @DisplayName("a skill id that is not an identifier is refused while decoding")
  void rejectsBadIds(final String id) {
    final ByteBuf buf = Unpooled.buffer();
    PacketCodecs.VAR_INT.encode(buf, 0);
    PacketCodecs.BOOL.encode(buf, true);
    PacketCodecs.STRING.encode(buf, id);
    assertThrows(DecoderException.class, () -> SetPowerSlotPayload.CODEC.decode(buf));
  }
}
