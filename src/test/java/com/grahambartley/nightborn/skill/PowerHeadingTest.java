package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.codec.PacketCodecs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class PowerHeadingTest {

  @ParameterizedTest(name = "forward {0}, back {1}, left {2}, right {3}: {4}")
  @CsvSource({
    "false, false, false, false, NONE",
    "true, false, false, false, FORWARD",
    "false, true, false, false, BACK",
    "false, false, true, false, LEFT",
    "false, false, false, true, RIGHT",
    "true, false, true, false, FORWARD",
    "false, true, false, true, BACK",
    "true, true, true, false, LEFT",
    "true, true, false, true, RIGHT",
    "false, false, true, true, NONE",
    "true, true, false, false, NONE",
    "true, true, true, true, NONE"
  })
  @DisplayName("opposite keys cancel, and forward or back wins over a side")
  void of(
      final boolean forward,
      final boolean back,
      final boolean left,
      final boolean right,
      final PowerHeading expected) {
    assertEquals(expected, PowerHeading.of(forward, back, left, right));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(PowerHeading.class)
  @DisplayName("every heading crosses the network unchanged")
  void roundTrips(final PowerHeading heading) {
    final ByteBuf buf = Unpooled.buffer();
    PowerHeading.PACKET_CODEC.encode(buf, heading);
    assertEquals(heading, PowerHeading.PACKET_CODEC.decode(buf));
  }

  @ParameterizedTest(name = "index {0}")
  @ValueSource(ints = {-1, 5, 1000})
  @DisplayName("a heading that does not exist is read as no heading")
  void unknownIsNone(final int index) {
    final ByteBuf buf = Unpooled.buffer();
    PacketCodecs.VAR_INT.encode(buf, index);
    assertEquals(PowerHeading.NONE, PowerHeading.PACKET_CODEC.decode(buf));
  }
}
