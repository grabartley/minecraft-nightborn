package com.grahambartley.nightborn.occult;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record MistState(long until, float harm) {

  public static final Codec<MistState> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.LONG.fieldOf("until").forGetter(MistState::until),
                      Codec.FLOAT.fieldOf("harm").forGetter(MistState::harm))
                  .apply(instance, MistState::new));

  public static final PacketCodec<ByteBuf, MistState> PACKET_CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_LONG,
          MistState::until,
          PacketCodecs.FLOAT,
          MistState::harm,
          MistState::new);

  public boolean over(final long now) {
    return now >= until;
  }
}
