package com.grahambartley.nightborn.network;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.function.ValueLists;

public enum PlayerCue {
  VOMIT,
  DRAIN,
  FLINCH,
  SMALL_FLINCH;

  private static final IntFunction<PlayerCue> BY_INDEX =
      ValueLists.createIdToValueFunction(
          PlayerCue::ordinal, values(), ValueLists.OutOfBoundsHandling.ZERO);

  public static final PacketCodec<ByteBuf, PlayerCue> PACKET_CODEC =
      PacketCodecs.indexed(BY_INDEX, PlayerCue::ordinal);
}
