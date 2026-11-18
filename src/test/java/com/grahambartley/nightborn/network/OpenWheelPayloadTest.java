package com.grahambartley.nightborn.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.wheel.WheelEntry;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OpenWheelPayloadTest {

  static RegistryByteBuf buffer() {
    return new RegistryByteBuf(Unpooled.buffer(), DynamicRegistryManager.EMPTY);
  }

  static WheelEntry entry(final int argument, final boolean current) {
    return new WheelEntry(
        Identifier.of("nightborn", "thrall_order"),
        argument,
        Identifier.ofVanilla("lead"),
        Text.translatable("nightborn.thrall.order.follow"),
        current);
  }

  @ParameterizedTest(name = "{0} with {1} entries")
  @CsvSource({"VAMPIRE, 0", "VAMPIRE, 3", "HUNTER, 16"})
  @DisplayName("a wheel's title, theme and entries cross the network unchanged")
  void roundTrips(final SkillTree theme, final int count) {
    final List<WheelEntry> entries = new ArrayList<>();
    for (int index = 0; index < count; index++) {
      entries.add(entry(index, index == 1));
    }
    final OpenWheelPayload payload =
        new OpenWheelPayload(42, theme, Text.literal("Old Tobias"), entries);
    final RegistryByteBuf buf = buffer();
    OpenWheelPayload.CODEC.encode(buf, payload);
    assertEquals(payload, OpenWheelPayload.CODEC.decode(buf));
  }

  @Test
  @DisplayName("a wheel with more entries than a ring holds is refused while decoding")
  void refusesTooMany() {
    final RegistryByteBuf buf = buffer();
    buf.writeVarInt(1);
    buf.writeVarInt(0);
    net.minecraft.text.TextCodecs.REGISTRY_PACKET_CODEC.encode(buf, Text.literal("t"));
    buf.writeVarInt(OpenWheelPayload.MOST_ENTRIES + 1);
    assertThrows(DecoderException.class, () -> OpenWheelPayload.CODEC.decode(buf));
  }
}
