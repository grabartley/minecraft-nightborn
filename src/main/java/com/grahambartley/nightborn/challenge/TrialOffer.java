package com.grahambartley.nightborn.challenge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public record TrialOffer(Optional<Side> side, long period, List<OfferedTrial> trials) {

  public static final long NO_PERIOD = -1L;

  public static final TrialOffer NONE = new TrialOffer(Optional.empty(), NO_PERIOD, List.of());

  public static final Codec<TrialOffer> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Side.CODEC.optionalFieldOf("side").forGetter(TrialOffer::side),
                      Codec.LONG.optionalFieldOf("period", NO_PERIOD).forGetter(TrialOffer::period),
                      OfferedTrial.CODEC
                          .listOf()
                          .optionalFieldOf("trials", List.of())
                          .forGetter(TrialOffer::trials))
                  .apply(instance, TrialOffer::new));

  public static final PacketCodec<ByteBuf, TrialOffer> PACKET_CODEC =
      PacketCodec.tuple(
          PacketCodecs.optional(PacketCodecs.indexed(i -> Side.values()[i], Side::ordinal)),
          TrialOffer::side,
          PacketCodecs.VAR_LONG,
          TrialOffer::period,
          OfferedTrial.PACKET_CODEC.collect(PacketCodecs.toList()),
          TrialOffer::trials,
          TrialOffer::new);

  public TrialOffer {
    trials = List.copyOf(trials);
  }

  public boolean current(final Optional<Side> playerSide, final long nowPeriod) {
    return side.equals(playerSide) && period == nowPeriod;
  }

  public Optional<OfferedTrial> find(final Identifier trial) {
    return trials.stream().filter(offered -> offered.trial().equals(trial)).findFirst();
  }

  public TrialOffer updating(final Identifier trial, final UnaryOperator<OfferedTrial> change) {
    final List<OfferedTrial> next = new ArrayList<>(trials.size());
    for (final OfferedTrial offered : trials) {
      next.add(offered.trial().equals(trial) ? change.apply(offered) : offered);
    }
    return new TrialOffer(side, period, next);
  }
}
