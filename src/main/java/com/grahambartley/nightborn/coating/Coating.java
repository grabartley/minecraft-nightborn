package com.grahambartley.nightborn.coating;

import com.grahambartley.nightborn.holy.HolyWater;
import com.grahambartley.nightborn.ward.Wards;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;

public enum Coating implements StringIdentifiable {
  HOLY_WATER("holy_water", HolyWater.WEAKENED_SOURCE),
  GARLIC("garlic", Wards.TOUCH);

  public static final Codec<Coating> CODEC = StringIdentifiable.createCodec(Coating::values);
  public static final PacketCodec<ByteBuf, Coating> PACKET_CODEC =
      PacketCodecs.indexed(index -> values()[index], Coating::ordinal);

  private final String id;
  private final String weakenedSource;

  Coating(final String id, final String weakenedSource) {
    this.id = id;
    this.weakenedSource = weakenedSource;
  }

  @Override
  public String asString() {
    return id;
  }

  public String weakenedSource() {
    return weakenedSource;
  }

  public String translationKey() {
    return "nightborn.coating." + id;
  }
}
