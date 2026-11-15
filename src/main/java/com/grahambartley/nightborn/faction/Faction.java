package com.grahambartley.nightborn.faction;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public enum Faction implements StringIdentifiable {
  HUMAN("human"),
  VAMPIRE("vampire");

  public static final Faction DEFAULT = HUMAN;

  public static final Codec<Faction> CODEC = StringIdentifiable.createCodec(Faction::values);

  private static final IntFunction<Faction> BY_INDEX =
      ValueLists.createIdToValueFunction(
          Faction::ordinal, values(), ValueLists.OutOfBoundsHandling.ZERO);

  public static final PacketCodec<ByteBuf, Faction> PACKET_CODEC =
      PacketCodecs.indexed(BY_INDEX, Faction::ordinal);

  private final String id;

  Faction(final String id) {
    this.id = id;
  }

  @Override
  public String asString() {
    return id;
  }
}
