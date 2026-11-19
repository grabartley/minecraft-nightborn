package com.grahambartley.nightborn.seeker;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.Identifier;

public record RevealPower(
    float range,
    float shareRange,
    int durationTicks,
    int colour,
    List<Identifier> bars,
    List<Veil> lifts) {

  public static final Codec<RevealPower> CODEC =
      RecordCodecBuilder.create(
          instance ->
              instance
                  .group(
                      Codec.floatRange(1.0F, 128.0F).fieldOf("range").forGetter(RevealPower::range),
                      Codec.floatRange(0.0F, 64.0F)
                          .fieldOf("share_range")
                          .forGetter(RevealPower::shareRange),
                      Codec.intRange(20, 6000)
                          .fieldOf("duration_ticks")
                          .forGetter(RevealPower::durationTicks),
                      Codec.intRange(0, 0xFFFFFF).fieldOf("colour").forGetter(RevealPower::colour),
                      Identifier.CODEC
                          .listOf()
                          .optionalFieldOf("bars", List.of())
                          .forGetter(RevealPower::bars),
                      Veil.CODEC
                          .listOf()
                          .optionalFieldOf("lifts", List.of())
                          .forGetter(RevealPower::lifts))
                  .apply(instance, RevealPower::new));

  public RevealPower {
    bars = List.copyOf(bars);
    lifts = List.copyOf(lifts);
  }
}
