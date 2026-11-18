package com.grahambartley.nightborn.village;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.util.Uuids;

public record TakenNeighbour(UUID owner, UUID servant) {

  public static final Codec<TakenNeighbour> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Uuids.INT_STREAM_CODEC.fieldOf("owner").forGetter(TakenNeighbour::owner),
                      Uuids.INT_STREAM_CODEC.fieldOf("servant").forGetter(TakenNeighbour::servant))
                  .apply(instance, TakenNeighbour::new));
}
