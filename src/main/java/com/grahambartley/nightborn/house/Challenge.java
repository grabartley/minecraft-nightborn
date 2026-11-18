package com.grahambartley.nightborn.house;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.util.Uuids;

public record Challenge(UUID challenger, long night) {

  public static final Codec<Challenge> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Uuids.CODEC.fieldOf("challenger").forGetter(Challenge::challenger),
                      Codec.LONG.fieldOf("night").forGetter(Challenge::night))
                  .apply(instance, Challenge::new));
}
