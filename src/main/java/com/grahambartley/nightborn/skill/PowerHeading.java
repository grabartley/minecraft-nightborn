package com.grahambartley.nightborn.skill;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.function.ValueLists;

public enum PowerHeading {
  NONE,
  FORWARD,
  BACK,
  LEFT,
  RIGHT;

  private static final IntFunction<PowerHeading> BY_INDEX =
      ValueLists.createIdToValueFunction(
          PowerHeading::ordinal, values(), ValueLists.OutOfBoundsHandling.ZERO);

  public static final PacketCodec<ByteBuf, PowerHeading> PACKET_CODEC =
      PacketCodecs.indexed(BY_INDEX, PowerHeading::ordinal);

  public static PowerHeading of(
      final boolean forward, final boolean back, final boolean left, final boolean right) {
    if (forward != back) {
      return forward ? FORWARD : BACK;
    }
    if (left != right) {
      return left ? LEFT : RIGHT;
    }
    return NONE;
  }
}
