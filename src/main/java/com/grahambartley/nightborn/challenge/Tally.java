package com.grahambartley.nightborn.challenge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record Tally(int total, List<String> seen, long night) {

  public static final long NO_NIGHT = -1L;

  public static final Tally EMPTY = new Tally(0, List.of(), NO_NIGHT);

  public static final Codec<Tally> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.INT.optionalFieldOf("total", 0).forGetter(Tally::total),
                      Codec.STRING
                          .listOf()
                          .optionalFieldOf("seen", List.of())
                          .forGetter(Tally::seen),
                      Codec.LONG.optionalFieldOf("night", NO_NIGHT).forGetter(Tally::night))
                  .apply(instance, Tally::new));

  public static final PacketCodec<ByteBuf, Tally> PACKET_CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_INT,
          Tally::total,
          PacketCodecs.STRING.collect(PacketCodecs.toList()),
          Tally::seen,
          PacketCodecs.VAR_LONG,
          Tally::night,
          Tally::new);

  public Tally {
    total = Math.max(0, total);
    seen = List.copyOf(new LinkedHashSet<>(seen));
  }

  public static Tally startingOn(final long night) {
    return new Tally(0, List.of(), night);
  }

  public Tally plus(final int amount) {
    return new Tally(saturatedAdd(total, amount), seen, night);
  }

  public Tally atLeast(final int amount) {
    return new Tally(Math.max(total, amount), seen, night);
  }

  public Tally seeing(final String value) {
    final Set<String> next = new LinkedHashSet<>(seen);
    next.add(value);
    return new Tally(total, List.copyOf(next), night);
  }

  private static int saturatedAdd(final int total, final int amount) {
    final long sum = (long) total + Math.max(0, amount);
    return (int) Math.min(Integer.MAX_VALUE, sum);
  }
}
