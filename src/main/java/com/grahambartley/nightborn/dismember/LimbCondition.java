package com.grahambartley.nightborn.dismember;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public enum LimbCondition implements StringIdentifiable {
  WHOLE("whole"),
  MISSING("missing"),
  REPLACED("replaced");

  public static final Codec<LimbCondition> CODEC =
      StringIdentifiable.createCodec(LimbCondition::values);

  private static final IntFunction<LimbCondition> BY_INDEX =
      ValueLists.createIdToValueFunction(
          LimbCondition::ordinal, values(), ValueLists.OutOfBoundsHandling.ZERO);

  public static final PacketCodec<ByteBuf, LimbCondition> PACKET_CODEC =
      PacketCodecs.indexed(BY_INDEX, LimbCondition::ordinal);

  private final String name;

  LimbCondition(final String name) {
    this.name = name;
  }

  @Override
  public String asString() {
    return name;
  }
}
