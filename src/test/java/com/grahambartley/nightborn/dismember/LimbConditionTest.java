package com.grahambartley.nightborn.dismember;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class LimbConditionTest {

  @ParameterizedTest(name = "{0} is saved as {1}")
  @CsvSource({"WHOLE, whole", "MISSING, missing", "REPLACED, replaced"})
  @DisplayName("each condition is saved by its name and read back")
  void savedByName(final LimbCondition condition, final String name) {
    assertEquals(
        new JsonPrimitive(name),
        LimbCondition.CODEC.encodeStart(JsonOps.INSTANCE, condition).getOrThrow());
    assertEquals(
        condition,
        LimbCondition.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(name)).getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(LimbCondition.class)
  @DisplayName("each condition crosses the network unchanged")
  void synced(final LimbCondition condition) {
    final PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
    LimbCondition.PACKET_CODEC.encode(buf, condition);
    assertEquals(condition, LimbCondition.PACKET_CODEC.decode(buf));
  }
}
