package com.grahambartley.nightborn.house;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.util.Uuids;

public record Challenge(UUID challenger, long night, int offered, long lastOffered) {

  public static final Codec<Challenge> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Uuids.CODEC.fieldOf("challenger").forGetter(Challenge::challenger),
                      Codec.LONG.fieldOf("night").forGetter(Challenge::night),
                      Codec.INT.optionalFieldOf("offered", 0).forGetter(Challenge::offered),
                      Codec.LONG
                          .optionalFieldOf("last_offered", Long.MIN_VALUE)
                          .forGetter(Challenge::lastOffered))
                  .apply(instance, Challenge::new));

  public static Challenge declared(final UUID challenger, final long night) {
    return new Challenge(challenger, night, 0, night);
  }

  public Challenge offeredOn(final long tonight) {
    return tonight > lastOffered && tonight > night
        ? new Challenge(challenger, night, offered + 1, tonight)
        : this;
  }
}
