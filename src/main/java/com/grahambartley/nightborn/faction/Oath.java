package com.grahambartley.nightborn.faction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record Oath(boolean proven, boolean sworn, int rank, int deeds) {

  public static final int TOP_RANK = 15;

  public static final Oath NONE = new Oath(false, false, 0, 0);

  public static final Codec<Oath> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.BOOL.optionalFieldOf("proven", false).forGetter(Oath::proven),
                      Codec.BOOL.optionalFieldOf("sworn", false).forGetter(Oath::sworn),
                      Codec.INT.optionalFieldOf("rank", 0).forGetter(Oath::rank),
                      Codec.INT.optionalFieldOf("deeds", 0).forGetter(Oath::deeds))
                  .apply(instance, Oath::new));

  public static final PacketCodec<ByteBuf, Oath> PACKET_CODEC =
      PacketCodec.tuple(
          PacketCodecs.BOOL,
          Oath::proven,
          PacketCodecs.BOOL,
          Oath::sworn,
          PacketCodecs.VAR_INT,
          Oath::rank,
          PacketCodecs.VAR_INT,
          Oath::deeds,
          Oath::new);

  public Oath {
    rank = Math.max(0, Math.min(TOP_RANK, rank));
    deeds = deeds & ((1 << (TOP_RANK + 1)) - 1);
  }

  public Oath prove() {
    return new Oath(true, sworn, rank, deeds);
  }

  public Oath swear() {
    return new Oath(true, true, rank, deeds);
  }

  public boolean hunter(final boolean vampire) {
    return !vampire && sworn;
  }

  public boolean sided(final boolean vampire) {
    return vampire || sworn;
  }

  public boolean maySwear(final boolean vampire) {
    return !vampire && proven && !sworn;
  }

  public Oath withRank(final int next) {
    return new Oath(proven, sworn, next, deeds);
  }

  public boolean deedDone(final int forRank) {
    return forRank >= 0 && forRank <= TOP_RANK && (deeds & (1 << forRank)) != 0;
  }

  public Oath withDeed(final int forRank) {
    if (forRank < 0 || forRank > TOP_RANK) {
      return this;
    }
    return new Oath(proven, sworn, rank, deeds | (1 << forRank));
  }
}
