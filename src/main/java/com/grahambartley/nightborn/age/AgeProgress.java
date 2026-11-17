package com.grahambartley.nightborn.age;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record AgeProgress(int rank, int nights, int blood, int deeds) {

  public static final int TOP_RANK = 15;
  public static final int MOST = 1_000_000;

  public static final AgeProgress EMPTY = new AgeProgress(0, 0, 0, 0);

  public static final Codec<AgeProgress> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.INT.optionalFieldOf("rank", 0).forGetter(AgeProgress::rank),
                      Codec.INT.optionalFieldOf("nights", 0).forGetter(AgeProgress::nights),
                      Codec.INT.optionalFieldOf("blood", 0).forGetter(AgeProgress::blood),
                      Codec.INT.optionalFieldOf("deeds", 0).forGetter(AgeProgress::deeds))
                  .apply(instance, AgeProgress::new));

  public static final PacketCodec<ByteBuf, AgeProgress> PACKET_CODEC =
      PacketCodec.tuple(
          PacketCodecs.VAR_INT,
          AgeProgress::rank,
          PacketCodecs.VAR_INT,
          AgeProgress::nights,
          PacketCodecs.VAR_INT,
          AgeProgress::blood,
          PacketCodecs.VAR_INT,
          AgeProgress::deeds,
          AgeProgress::new);

  public AgeProgress {
    rank = Math.max(0, Math.min(TOP_RANK, rank));
    nights = Math.max(0, Math.min(MOST, nights));
    blood = Math.max(0, Math.min(MOST, blood));
    deeds = deeds & ((1 << (TOP_RANK + 1)) - 1);
  }

  public AgeProgress withRank(final int next) {
    return new AgeProgress(next, nights, blood, deeds);
  }

  public AgeProgress lived(final int more) {
    return new AgeProgress(
        rank, (int) Math.min(MOST, (long) nights + Math.max(0, more)), blood, deeds);
  }

  public AgeProgress drank(final int points) {
    return new AgeProgress(
        rank, nights, (int) Math.min(MOST, (long) blood + Math.max(0, points)), deeds);
  }

  public boolean deedDone(final int forRank) {
    return forRank >= 0 && forRank <= TOP_RANK && (deeds & (1 << forRank)) != 0;
  }

  public AgeProgress withDeed(final int forRank) {
    if (forRank < 0 || forRank > TOP_RANK) {
      return this;
    }
    return new AgeProgress(rank, nights, blood, deeds | (1 << forRank));
  }
}
