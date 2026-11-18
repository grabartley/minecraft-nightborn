package com.grahambartley.nightborn.moment;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public enum Moment implements StringIdentifiable {
  TURNING("vampire", true),
  OATH("hunter", false);

  public static final Codec<Moment> CODEC = StringIdentifiable.createCodec(Moment::values);

  private static final IntFunction<Moment> BY_INDEX =
      ValueLists.createIdToValueFunction(
          Moment::ordinal, values(), ValueLists.OutOfBoundsHandling.ZERO);

  public static final PacketCodec<ByteBuf, Moment> PACKET_CODEC =
      PacketCodecs.indexed(BY_INDEX, Moment::ordinal);

  private final String becomes;
  private final boolean seenOnlyByVampires;

  Moment(final String becomes, final boolean seenOnlyByVampires) {
    this.becomes = becomes;
    this.seenOnlyByVampires = seenOnlyByVampires;
  }

  @Override
  public String asString() {
    return becomes;
  }

  public String cardKey() {
    return "nightborn.moment." + becomes;
  }

  public boolean seenBy(final boolean viewerIsVampire, final double distanceSq, final int range) {
    return (viewerIsVampire || !seenOnlyByVampires) && distanceSq <= (double) range * range;
  }
}
