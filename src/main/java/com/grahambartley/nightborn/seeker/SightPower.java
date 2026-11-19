package com.grahambartley.nightborn.seeker;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record SightPower(float range, int durationTicks, int colour, List<Veil> seesThrough) {

  public static final Codec<SightPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, 128.0F).fieldOf("range").forGetter(SightPower::range),
                      Codec.intRange(20, 6000)
                          .fieldOf("duration_ticks")
                          .forGetter(SightPower::durationTicks),
                      Codec.intRange(0, 0xFFFFFF).fieldOf("colour").forGetter(SightPower::colour),
                      Veil.CODEC
                          .listOf()
                          .optionalFieldOf("sees_through", List.of())
                          .forGetter(SightPower::seesThrough))
                  .apply(instance, SightPower::new));

  public SightPower {
    seesThrough = List.copyOf(seesThrough);
  }
}
