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
    TrialOffer offer, List<Identifier> finished, List<TrialOffer> shelved, FeatLog feats) {

  public static final ChallengeRecord EMPTY =
      new ChallengeRecord(TrialOffer.NONE, List.of(), List.of(), FeatLog.EMPTY);

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
                      TrialOffer.CODEC
                          .listOf()
                          .optionalFieldOf("shelved", List.of())
                          .forGetter(ChallengeRecord::shelved),
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
          TrialOffer.PACKET_CODEC.collect(PacketCodecs.toList()),
          ChallengeRecord::shelved,
          FeatLog.PACKET_CODEC,
          ChallengeRecord::feats,
          ChallengeRecord::new);

  public ChallengeRecord {
    finished = List.copyOf(new LinkedHashSet<>(finished));
    shelved = List.copyOf(shelved);
  }

  public boolean everFinished(final Identifier trial) {
    return finished.contains(trial);
  }

  public ChallengeRecord withOffer(final TrialOffer next) {
    return new ChallengeRecord(next, finished, shelved, feats);
  }

  public ChallengeRecord withFeats(final FeatLog next) {
    return new ChallengeRecord(offer, finished, shelved, next);
  }

  public ChallengeRecord finishing(final Identifier trial) {
    final Set<Identifier> next = new LinkedHashSet<>(finished);
    next.add(trial);
    return new ChallengeRecord(offer, List.copyOf(next), shelved, feats);
  }

  public ChallengeRecord withShelved(final TrialOffer next, final List<TrialOffer> kept) {
    return new ChallengeRecord(next, finished, kept, feats);
  }

  public boolean isEmpty() {
    return equals(EMPTY);
  }
}
