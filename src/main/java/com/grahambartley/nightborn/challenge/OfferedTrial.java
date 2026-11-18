package com.grahambartley.nightborn.challenge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public record OfferedTrial(Identifier trial, boolean taken, boolean done, Tally tally) {

  public static final Codec<OfferedTrial> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Identifier.CODEC.fieldOf("trial").forGetter(OfferedTrial::trial),
                      Codec.BOOL.optionalFieldOf("taken", false).forGetter(OfferedTrial::taken),
                      Codec.BOOL.optionalFieldOf("done", false).forGetter(OfferedTrial::done),
                      Tally.CODEC
                          .optionalFieldOf("tally", Tally.EMPTY)
                          .forGetter(OfferedTrial::tally))
                  .apply(instance, OfferedTrial::new));

  public static final PacketCodec<ByteBuf, OfferedTrial> PACKET_CODEC =
      PacketCodec.tuple(
          Identifier.PACKET_CODEC,
          OfferedTrial::trial,
          PacketCodecs.BOOL,
          OfferedTrial::taken,
          PacketCodecs.BOOL,
          OfferedTrial::done,
          Tally.PACKET_CODEC,
          OfferedTrial::tally,
          OfferedTrial::new);

  public static OfferedTrial fresh(final Identifier trial, final boolean taken) {
    return new OfferedTrial(trial, taken, false, Tally.EMPTY);
  }

  public boolean counting() {
    return taken && !done;
  }

  public OfferedTrial taking() {
    return new OfferedTrial(trial, true, done, Tally.EMPTY);
  }

  public OfferedTrial counted(final Tally next, final boolean finished) {
    return new OfferedTrial(trial, taken, done || finished, next);
  }
}
