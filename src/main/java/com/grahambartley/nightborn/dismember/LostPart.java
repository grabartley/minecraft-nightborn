package com.grahambartley.nightborn.dismember;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record LostPart(
    LimbCondition condition, long lostAt, int bleedTicks, Optional<Integer> regrowTicks) {

  public static final Codec<LostPart> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      LimbCondition.CODEC.fieldOf("condition").forGetter(LostPart::condition),
                      Codec.LONG.fieldOf("lost_at").forGetter(LostPart::lostAt),
                      Codec.intRange(0, Integer.MAX_VALUE)
                          .optionalFieldOf("bleed_ticks", 0)
                          .forGetter(LostPart::bleedTicks),
                      Codec.intRange(0, Integer.MAX_VALUE)
                          .optionalFieldOf("regrow_ticks")
                          .forGetter(LostPart::regrowTicks))
                  .apply(instance, LostPart::new));

  public static final PacketCodec<ByteBuf, LostPart> PACKET_CODEC =
      PacketCodec.tuple(
          LimbCondition.PACKET_CODEC,
          LostPart::condition,
          PacketCodecs.VAR_LONG,
          LostPart::lostAt,
          PacketCodecs.VAR_INT,
          LostPart::bleedTicks,
          PacketCodecs.optional(PacketCodecs.VAR_INT),
          LostPart::regrowTicks,
          LostPart::new);

  public static LostPart missing(final long now) {
    return new LostPart(LimbCondition.MISSING, now, 0, Optional.empty());
  }
}
