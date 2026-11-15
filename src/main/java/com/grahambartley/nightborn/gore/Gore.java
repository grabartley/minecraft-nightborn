package com.grahambartley.nightborn.gore;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.function.ValueLists;

public enum Gore {
  BITE,
  STAKE,
  SILVER,
  DRAINED,
  STAKED;

  private static final IntFunction<Gore> BY_INDEX =
      ValueLists.createIdToValueFunction(
          Gore::ordinal, values(), ValueLists.OutOfBoundsHandling.ZERO);

  public static final PacketCodec<ByteBuf, Gore> PACKET_CODEC =
      PacketCodecs.indexed(BY_INDEX, Gore::ordinal);
}
