package com.grahambartley.nightborn.challenge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

public record ChallengeRecord(
    TrialOffer offer, List<Identifier> finished, PaidTrials paid, FeatLog feats) {

  public record PaidTrials(long period, List<Identifier> trials) {

    public static final PaidTrials NONE = new PaidTrials(TrialOffer.NO_PERIOD, List.of());

    public static final Codec<PaidTrials> CODEC =
        RecordCodecBuilder.create(
            instance ->
                instance
                    .group(
                        Codec.LONG
                            .optionalFieldOf("period", TrialOffer.NO_PERIOD)
                            .forGetter(PaidTrials::period),
                        Identifier.CODEC
                            .listOf()
                            .optionalFieldOf("trials", List.of())
                            .forGetter(PaidTrials::trials))
                    .apply(instance, PaidTrials::new));

    public static final PacketCodec<ByteBuf, PaidTrials> PACKET_CODEC =
        PacketCodec.tuple(
            PacketCodecs.VAR_LONG,
            PaidTrials::period,
            Identifier.PACKET_CODEC.collect(PacketCodecs.toList()),
            PaidTrials::trials,
            PaidTrials::new);

    public PaidTrials {
      trials = List.copyOf(new LinkedHashSet<>(trials));
    }

    public Set<Identifier> in(final long now) {
      return now == period ? Set.copyOf(trials) : Set.of();
    }

    PaidTrials with(final long now, final Identifier trial) {
      final Set<Identifier> next = new LinkedHashSet<>(in(now));
      next.add(trial);
      return new PaidTrials(now, List.copyOf(next));
    }
  }

  public static final ChallengeRecord EMPTY =
      new ChallengeRecord(TrialOffer.NONE, List.of(), PaidTrials.NONE, FeatLog.EMPTY);

  public static final Codec<ChallengeRecord> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      TrialOffer.CODEC
                          .optionalFieldOf("offer", TrialOffer.NONE)
                          .forGetter(ChallengeRecord::offer),
                      Identifier.CODEC
                          .listOf()
                          .optionalFieldOf("finished", List.of())
                          .forGetter(ChallengeRecord::finished),
                      PaidTrials.CODEC
                          .optionalFieldOf("paid", PaidTrials.NONE)
                          .forGetter(ChallengeRecord::paid),
                      FeatLog.CODEC
                          .optionalFieldOf("feats", FeatLog.EMPTY)
                          .forGetter(ChallengeRecord::feats))
                  .apply(instance, ChallengeRecord::new));

  public static final PacketCodec<ByteBuf, ChallengeRecord> PACKET_CODEC =
      PacketCodec.tuple(
          TrialOffer.PACKET_CODEC,
          ChallengeRecord::offer,
          Identifier.PACKET_CODEC.collect(PacketCodecs.toList()),
          ChallengeRecord::finished,
          PaidTrials.PACKET_CODEC,
          ChallengeRecord::paid,
          FeatLog.PACKET_CODEC,
          ChallengeRecord::feats,
          ChallengeRecord::new);

  public ChallengeRecord {
    finished = List.copyOf(new LinkedHashSet<>(finished));
  }

  public boolean everFinished(final Identifier trial) {
    return finished.contains(trial);
  }

  public ChallengeRecord withOffer(final TrialOffer next) {
    return new ChallengeRecord(next, finished, paid, feats);
  }

  public ChallengeRecord withFeats(final FeatLog next) {
    return new ChallengeRecord(offer, finished, paid, next);
  }

  public ChallengeRecord finishing(final Identifier trial) {
    final Set<Identifier> next = new LinkedHashSet<>(finished);
    next.add(trial);
    return new ChallengeRecord(offer, List.copyOf(next), paid.with(offer.period(), trial), feats);
  }

  public boolean isEmpty() {
    return equals(EMPTY);
  }
}
