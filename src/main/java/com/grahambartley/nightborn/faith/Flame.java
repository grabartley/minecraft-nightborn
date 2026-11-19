package com.grahambartley.nightborn.faith;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record Flame(long until, float damage, int burnTicks) {

  public static final Codec<Flame> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.LONG.fieldOf("until").forGetter(Flame::until),
                      Codec.FLOAT.fieldOf("damage").forGetter(Flame::damage),
                      Codec.INT.fieldOf("burn_ticks").forGetter(Flame::burnTicks))
                  .apply(instance, Flame::new));

  public static final PacketCodec<ByteBuf, Flame> PACKET_CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_LONG,
          Flame::until,
          PacketCodecs.FLOAT,
          Flame::damage,
          PacketCodecs.VAR_INT,
          Flame::burnTicks,
          Flame::new);

  public boolean burning(final long now) {
    return now < until;
  }

  public long ticksLeft(final long now) {
    return Math.max(0L, until - now);
  }
}
